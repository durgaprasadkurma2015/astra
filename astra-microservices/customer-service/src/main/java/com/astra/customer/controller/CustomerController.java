
package com.astra.customer.controller;

import com.astra.customer.dto.CustomerRequest;
import com.astra.customer.dto.CustomerResponse;
import com.astra.customer.service.CustomerManagementService;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/customers")
public class CustomerController {

    private final CustomerManagementService service;

    public CustomerController(CustomerManagementService service) {
        this.service = service;
    }

    // CUSTOMER creates their own profile
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CustomerResponse> create(
            @Valid @RequestBody CustomerRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = jwt.getClaim("userId");

        return ResponseEntity
            .status(HttpStatus.CREATED)
            .body(service.create(request, userId));
    }

    // ADMIN only
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<CustomerResponse>> getAll() {

        return ResponseEntity.ok(service.getAll(true));
    }

    // CUSTOMER: own profile
    // ADMIN: any customer
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<CustomerResponse> getById(
            @PathVariable Long id,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = jwt.getClaim("userId");
        boolean isAdmin = jwt.getClaimAsString("role").equals("ADMIN");

        return ResponseEntity.ok(
            service.getById(id, userId, isAdmin)
        );
    }

    // CUSTOMER: own profile
    // ADMIN: any customer
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('CUSTOMER', 'ADMIN')")
    public ResponseEntity<CustomerResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody CustomerRequest request,
            @AuthenticationPrincipal Jwt jwt) {

        Long userId = jwt.getClaim("userId");
        boolean isAdmin = jwt.getClaimAsString("role").equals("ADMIN");

        return ResponseEntity.ok(
            service.update(id, request, userId, isAdmin)
        );
    }

    // ADMIN only
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> delete(@PathVariable Long id) {

        service.delete(id, true);

        return ResponseEntity.noContent().build();
    }
    
 // CUSTOMER: Get logged-in customer's own profile
    @GetMapping("/me")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<CustomerResponse> getMyProfile(
            @AuthenticationPrincipal Jwt jwt) {

        Long authUserId = jwt.getClaim("userId");

        return ResponseEntity.ok(
            service.getByAuthUserId(authUserId)
        );
    }
}
