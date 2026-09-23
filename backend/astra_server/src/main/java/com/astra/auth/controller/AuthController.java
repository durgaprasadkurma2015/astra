package com.astra.auth.controller;

import com.astra.auth.dto.*;
import com.astra.auth.service.AuthService;
import com.astra.dto.AuthResponse;
import com.astra.dto.MessageResponse;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(
            @Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponse> verifyOtp(
            @Valid @RequestBody VerifyOtpRequest request) {
        return ResponseEntity.ok(authService.verifyEmailOtp(request));
    }

    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(
            @Valid @RequestBody RefreshTokenRequest request) {

        return ResponseEntity.ok(
                authService.refresh(request.refreshToken())
        );
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(
            @Valid @RequestBody LogoutRequest request) {

        return ResponseEntity.ok(
                authService.logout(request.refreshToken())
        );
    }

    // ---------------- SMS OTP ----------------

    @PostMapping("/sms/send-otp")
    public ResponseEntity<MessageResponse> sendSmsOtp(
            @Valid @RequestBody SendSmsOtpRequest request) {

        return ResponseEntity.ok(
                authService.sendSmsOtp(request.phone())
        );
    }

    @PostMapping("/sms/verify-otp")
    public ResponseEntity<AuthResponse> verifySmsOtp(
            @Valid @RequestBody VerifySmsOtpRequest request) {

        return ResponseEntity.ok(
                authService.verifySmsOtp(
                        request.phone(),
                        request.otp()
                )
        );
    }

    // ---------------- Password ----------------

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request) {
        return ResponseEntity.ok(authService.forgotPassword(request));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(
            @Valid @RequestBody ResetPasswordRequest request) {
        return ResponseEntity.ok(authService.resetPassword(request));
    }

    // ---------------- Google ----------------

    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleLogin(
            @Valid @RequestBody GoogleLoginRequest request) {
        return ResponseEntity.ok(authService.googleLogin(request));
    }
}
