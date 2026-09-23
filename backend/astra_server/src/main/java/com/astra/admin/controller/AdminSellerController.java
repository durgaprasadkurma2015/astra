package com.astra.admin.controller;

import com.astra.admin.service.AdminSellerService;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/sellers")
@PreAuthorize("hasRole('ADMIN')")
public class AdminSellerController {

private final AdminSellerService sellerService;

public AdminSellerController(
        AdminSellerService sellerService
) {
    this.sellerService = sellerService;
}

@GetMapping
public ResponseEntity<Page<?>> getSellers(
        @RequestParam(required = false) String status,
        Pageable pageable
) {

    return ResponseEntity.ok(
            sellerService.getSellers(
                    status,
                    pageable
            )
    );
}

@GetMapping("/{sellerId}")
public ResponseEntity<?> getSeller(
        @PathVariable Long sellerId
) {

    return ResponseEntity.ok(
            sellerService.getSeller(sellerId)
    );
}

@PutMapping("/{sellerId}/approve")
public ResponseEntity<?> approve(
        @PathVariable Long sellerId
) {

    return ResponseEntity.ok(
            sellerService.approve(sellerId)
    );
}

@PutMapping("/{sellerId}/reject")
public ResponseEntity<?> reject(
        @PathVariable Long sellerId
) {

    return ResponseEntity.ok(
            sellerService.reject(sellerId)
    );
}

@PutMapping("/{sellerId}/suspend")
public ResponseEntity<?> suspend(
        @PathVariable Long sellerId
) {

    return ResponseEntity.ok(
            sellerService.suspend(sellerId)
    );
}

@PutMapping("/{sellerId}/activate")
public ResponseEntity<?> activate(
        @PathVariable Long sellerId
) {

    return ResponseEntity.ok(
            sellerService.activate(sellerId)
    );
}

@PutMapping("/{sellerId}/close")
public ResponseEntity<?> close(
        @PathVariable Long sellerId
) {

    return ResponseEntity.ok(
            sellerService.close(sellerId)
    );
}

}