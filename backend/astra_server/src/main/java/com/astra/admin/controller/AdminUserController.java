package com.astra.admin.controller;

import com.astra.admin.dto.AdminUserResponse;
import com.astra.admin.service.AdminUserService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/users")
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService userService;

    public AdminUserController(
            AdminUserService userService
    ) {
        this.userService = userService;
    }

    @GetMapping
    public ResponseEntity<Page<AdminUserResponse>> getUsers(
            Pageable pageable
    ) {

        return ResponseEntity.ok(
                userService.getUsers(pageable)
        );
    }

    @GetMapping("/{userId}")
    public ResponseEntity<AdminUserResponse> getUser(
            @PathVariable Long userId
    ) {

        return ResponseEntity.ok(
                userService.getUser(userId)
        );
    }

    @PutMapping("/{userId}/enable")
    public ResponseEntity<AdminUserResponse> enable(
            @PathVariable Long userId
    ) {

        return ResponseEntity.ok(
                userService.enable(userId)
        );
    }

    @PutMapping("/{userId}/disable")
    public ResponseEntity<AdminUserResponse> disable(
            @PathVariable Long userId
    ) {

        return ResponseEntity.ok(
                userService.disable(userId)
        );
    }
}