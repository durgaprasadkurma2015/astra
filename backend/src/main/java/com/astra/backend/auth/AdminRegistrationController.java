package com.astra.backend.auth;

import com.astra.backend.auth.dto.AdminRegistrationRequest;
import com.astra.backend.auth.dto.AdminRegistrationResponse;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/admins")
public class AdminRegistrationController {

    private final AdminRegistrationService adminRegistrationService;

    public AdminRegistrationController(
            AdminRegistrationService adminRegistrationService) {
        this.adminRegistrationService = adminRegistrationService;
    }

    @PostMapping
    public ResponseEntity<AdminRegistrationResponse> registerAdmin(
            @Valid @RequestBody AdminRegistrationRequest request,
            Authentication authentication) {

        AdminRegistrationResponse response =
                adminRegistrationService.registerAdmin(
                        request,
                        authentication.getName()
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }
}