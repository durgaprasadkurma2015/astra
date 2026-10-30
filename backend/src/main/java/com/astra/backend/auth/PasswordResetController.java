package com.astra.backend.auth;

import java.util.Map;

import com.astra.backend.auth.dto.ForgotPasswordRequest;
import com.astra.backend.auth.dto.ResetPasswordRequest;
import com.astra.backend.auth.dto.VerifyOtpRequest;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(
        PasswordResetService passwordResetService
    ) {
        this.passwordResetService = passwordResetService;
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
        @Valid @RequestBody ForgotPasswordRequest request
    ) {
        passwordResetService.requestOtp(request);

        return ResponseEntity.ok(Map.of(
            "message",
            "If an eligible account exists, an OTP will be sent to its email."
        ));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, String>> verifyOtp(
        @Valid @RequestBody VerifyOtpRequest request
    ) {
        String resetToken = passwordResetService.verifyOtp(request);

        return ResponseEntity.ok(Map.of(
            "resetToken", resetToken,
            "message", "OTP verified successfully."
        ));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
        @Valid @RequestBody ResetPasswordRequest request
    ) {
        passwordResetService.resetPassword(request);

        return ResponseEntity.ok(Map.of(
            "message", "Password reset successfully."
        ));
    }
}