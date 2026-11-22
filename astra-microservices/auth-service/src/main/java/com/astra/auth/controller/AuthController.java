package com.astra.auth.controller;

import com.astra.auth.dto.RegisterRequest;
import com.astra.auth.dto.ResendRegistrationOtpRequest;
import com.astra.auth.dto.VerifyRegistrationOtpRequest;
import com.astra.auth.service.AuthService;
import com.astra.auth.dto.ResetPasswordRequest;
import com.astra.auth.dto.ChangePasswordRequest;
import com.astra.auth.security.CustomUserDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.astra.auth.dto.ForgotPasswordRequest;
import com.astra.auth.dto.LoginRequest;
import com.astra.auth.dto.LoginResponse;

import java.util.HashMap;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import com.astra.auth.security.CustomUserDetails;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {
	
	

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    @GetMapping("/test")
    public Map<String, String> test() {
        return Map.of(
            "service", "Auth Service",
            "status", "Working"
        );
    }
 // ------------------------------------------------
 // LOGOUT
 // ------------------------------------------------

 @PostMapping("/logout")
 public ResponseEntity<?> logout(HttpServletRequest request) {

     String authorizationHeader =
             request.getHeader("Authorization");

     if (authorizationHeader == null ||
             !authorizationHeader.startsWith("Bearer ")) {

         throw new ResponseStatusException(
                 HttpStatus.UNAUTHORIZED,
                 "Bearer token is required"
         );
     }

     String token = authorizationHeader.substring(7);

     String message = authService.logout(token);

     return ResponseEntity.ok(
             Map.of("message", message)
     );
 }
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @Valid @RequestBody ChangePasswordRequest request,
            @AuthenticationPrincipal CustomUserDetails currentUser) {

        String message = authService.changePassword(request, currentUser);

        return ResponseEntity.ok(Map.of("message", message));
    }
    
    @PostMapping("/forgot-password")
    public ResponseEntity<?> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {

        return ResponseEntity.ok(
                Map.of("message", authService.forgotPassword(request))
        );
    }
    
    @GetMapping("/me")
    @SecurityRequirement(name = "bearerAuth")
    public ResponseEntity<?> getCurrentUser(
            @AuthenticationPrincipal CustomUserDetails userDetails) {

        if (userDetails == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Authentication required"));
        }

        Map<String, Object> response = new HashMap<>();
        response.put("id", userDetails.getId());
        response.put("email", userDetails.getEmail());
        response.put("role", userDetails.getRole());
        response.put("enabled", userDetails.isEnabled());

        return ResponseEntity.ok(response);
    }

@PostMapping("/reset-password")
public ResponseEntity<?> resetPassword(
        @Valid @RequestBody ResetPasswordRequest request) {

    String message = authService.resetPassword(request);

    return ResponseEntity.ok(
            Map.of("message", message)
    );
}


    @PostMapping("/register")
    public ResponseEntity<Map<String, String>> register(
            @Valid @RequestBody RegisterRequest request) {

        String message = authService.register(request);

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(Map.of("message", message));
    }
    
    @PostMapping("/verify-registration-otp")
    public ResponseEntity<Map<String, String>> verifyRegistrationOtp(
            @Valid @RequestBody VerifyRegistrationOtpRequest request) {

        String message = authService.verifyRegistrationOtp(request);

        return ResponseEntity.ok(Map.of("message", message));
    }

    @PostMapping("/resend-registration-otp")
    public ResponseEntity<Map<String, String>> resendRegistrationOtp(
            @Valid @RequestBody ResendRegistrationOtpRequest request) {

        String message = authService.resendRegistrationOtp(request);

        return ResponseEntity.ok(Map.of("message", message));
    }
    
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest request) {

        return ResponseEntity.ok(
            authService.login(request)
        );
    }
}