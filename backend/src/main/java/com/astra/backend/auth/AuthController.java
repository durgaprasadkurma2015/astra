
package com.astra.backend.auth;

import java.util.Map;

import com.astra.backend.audit.AuditService;
import com.astra.backend.auth.dto.AuthResponse;
import com.astra.backend.auth.dto.LoginRequest;
import com.astra.backend.auth.dto.RegisterRequest;
import com.astra.backend.auth.dto.VerifyRegistrationOtpRequest;
import com.astra.backend.auth.dto.ResendRegistrationOtpRequest;
import com.astra.backend.auth.dto.ChangePasswordRequest;
import com.astra.backend.user.dto.UpdateProfileRequest;
import com.astra.backend.user.dto.UserProfileResponse;


import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;



@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private static final Logger log =
            LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final AuditService auditService;
    private final RefreshCookieService refreshCookieService;

    public AuthController(
            AuthService authService,
            AuditService auditService,
            RefreshCookieService refreshCookieService
    ) {
        this.authService = authService;
        this.auditService = auditService;
        this.refreshCookieService = refreshCookieService;
    }

    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(
            @Valid @RequestBody RegisterRequest request,
            HttpServletRequest httpRequest
    ) {
        try {
            String message = authService.register(request);

            auditSafely(
                    request.email(),
                    "REGISTER",
                    "SUCCESS",
                    "Registration started",
                    httpRequest
            );

            return ResponseEntity.status(HttpStatus.CREATED)
                    .body(Map.of("message", message));

        } catch (RuntimeException ex) {
            auditSafely(
                    request.email(),
                    "REGISTER",
                    "FAILURE",
                    "Registration failed",
                    httpRequest
            );
            throw ex;
        }
    }

    @PostMapping("/verify-registration-otp")
    public ResponseEntity<Map<String, String>> verifyRegistrationOtp(
            @Valid @RequestBody VerifyRegistrationOtpRequest request,
            HttpServletRequest httpRequest
    ) {
        boolean verified;

        try {
            verified = authService.verifyRegistrationOtp(
                    request.email(),
                    request.otp()
            );
        } catch (RuntimeException ex) {
            auditSafely(
                    request.email(),
                    "REGISTRATION_OTP_VERIFY",
                    "FAILURE",
                    "OTP verification processing failed",
                    httpRequest
            );
            throw ex;
        }

        if (!verified) {
            auditSafely(
                    request.email(),
                    "REGISTRATION_OTP_VERIFY",
                    "FAILURE",
                    "Invalid, expired or unavailable OTP",
                    httpRequest
            );

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid or expired OTP. Please check the code or request a new one."
            );
        }

        auditSafely(
                request.email(),
                "REGISTRATION_OTP_VERIFY",
                "SUCCESS",
                "Email verified",
                httpRequest
        );

        return ResponseEntity.ok(Map.of(
                "message",
                "Email verified successfully. You can now log in."
        ));
    }

    @PostMapping("/resend-registration-otp")
    public ResponseEntity<Map<String, String>> resendRegistrationOtp(
            @Valid @RequestBody ResendRegistrationOtpRequest request,
            HttpServletRequest httpRequest
    ) {
        try {
            String message =
                    authService.resendRegistrationOtp(request.email());

            auditSafely(
                    request.email(),
                    "REGISTRATION_OTP_RESEND",
                    "SUCCESS",
                    "OTP resend processed",
                    httpRequest
            );

            return ResponseEntity.ok(Map.of("message", message));

        } catch (RuntimeException ex) {
            auditSafely(
                    request.email(),
                    "REGISTRATION_OTP_RESEND",
                    "FAILURE",
                    "OTP resend failed",
                    httpRequest
            );
            throw ex;
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest
            
    ) {
        try {
            AuthService.AuthSession session = authService.login(request);

            auditSafely(
                    request.email(),
                    "LOGIN",
                    "SUCCESS",
                    "Authentication successful",
                    httpRequest
            );

            return ResponseEntity.ok()
                    .header(HttpHeaders.SET_COOKIE,
                            refreshCookieService.issue(session).toString())
                    .body(session.response());

        } catch (RuntimeException ex) {
            auditSafely(
                    request.email(),
                    "LOGIN",
                    "FAILURE",
                    "Authentication failed",
                    httpRequest
            );
            throw ex;
        }
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(HttpServletRequest request) {
        AuthService.AuthSession session = authService.refresh(
                refreshCookieService.read(request));
        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE,
                        refreshCookieService.issue(session).toString())
                .body(session.response());
        
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        authService.revokeRefreshToken(refreshCookieService.read(request));
        return ResponseEntity.noContent()
                .header(HttpHeaders.SET_COOKIE,
                        refreshCookieService.clear().toString())
                .build();
    }

    @GetMapping("/me")
    public UserProfileResponse me(Authentication authentication) {
        return authService.getProfile(authentication.getName());
    }

    @PutMapping("/me")
    public UserProfileResponse updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request,
            HttpServletRequest httpRequest
    ) {
        String email = authentication.getName();

        try {
            UserProfileResponse response =
                    authService.updateProfile(email, request);

            auditSafely(
                    email,
                    "PROFILE_UPDATE",
                    "SUCCESS",
                    "Profile updated",
                    httpRequest
            );

            return response;

        } catch (RuntimeException ex) {
            auditSafely(
                    email,
                    "PROFILE_UPDATE",
                    "FAILURE",
                    "Profile update failed",
                    httpRequest
            );
            throw ex;
        }
    }

    @PostMapping("/change-password")
    public ResponseEntity<Map<String, String>> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request,
            HttpServletRequest httpRequest
    ) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of(
                            "message",
                            "Authentication required. Please log in again."
                    ));
        }

        String email = authentication.getName();

        try {
            String message = authService.changePassword(email, request);

            auditSafely(
                    email,
                    "CHANGE_PASSWORD",
                    "SUCCESS",
                    "Password changed",
                    httpRequest
            );

            return ResponseEntity.ok(Map.of("message", message));

        } catch (RuntimeException ex) {
            auditSafely(
                    email,
                    "CHANGE_PASSWORD",
                    "FAILURE",
                    "Password change failed",
                    httpRequest
            );
            throw ex;
        }
    }

    private void auditSafely(
            String email,
            String action,
            String outcome,
            String details,
            HttpServletRequest request
    ) {
        try {
            auditService.log(
                    email,
                    action,
                    outcome,
                    details,
                    request.getRemoteAddr()
            );
        } catch (RuntimeException auditException) {
            // Do not replace the original authentication/API result
            // with an audit persistence error.
            log.error(
                    "Could not persist audit event: action={}, outcome={}",
                    action,
                    outcome,
                    auditException
            );
        }
        
    }
    
    
}
