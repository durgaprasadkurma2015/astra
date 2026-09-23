package com.astra.admin.controller;

import com.astra.admin.dto.AdminDashboardResponse;
import com.astra.admin.service.AdminDashboardService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

private final AdminDashboardService dashboardService;

public AdminController(
        AdminDashboardService dashboardService
) {
    this.dashboardService = dashboardService;
}

@GetMapping("/dashboard")
public ResponseEntity<AdminDashboardResponse> dashboard() {

    return ResponseEntity.ok(
            dashboardService.getDashboard()
    );
}

}