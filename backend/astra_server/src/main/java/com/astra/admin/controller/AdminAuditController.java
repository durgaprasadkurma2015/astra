package com.astra.admin.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.astra.admin.service.AdminAuditService;

@RestController
@RequestMapping("/api/v1/admin/audit")
public class AdminAuditController {

    private final AdminAuditService auditService;

    public AdminAuditController(
            AdminAuditService auditService) {
        this.auditService = auditService;
    }

    @GetMapping
    public ResponseEntity<?> getAuditLogs(
            @RequestParam(required = false) Long userId,
            @RequestParam(required = false) String action,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "50") int size) {

        return ResponseEntity.ok(
                auditService.getAuditLogs(
                        userId,
                        action,
                        page,
                        size
                )
        );
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getAuditLog(
            @PathVariable Long id) {

        return ResponseEntity.ok(
                auditService.getAuditLog(id)
        );
    }
}
