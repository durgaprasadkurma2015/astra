package com.astra.auth.controller;

import com.astra.auth.dto.RegisterRequest;
import com.astra.auth.dto.ResendRegistrationOtpRequest;
import com.astra.auth.dto.VerifyRegistrationOtpRequest;
import com.astra.auth.service.AuthService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.astra.auth.dto.LoginRequest;
import com.astra.auth.dto.LoginResponse;

import java.util.Map;

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