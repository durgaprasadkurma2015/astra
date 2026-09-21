package com.astra.seller.controller;

import com.astra.seller.dto.SellerRegistrationRequest;
import com.astra.seller.dto.SellerResponse;
import com.astra.seller.service.SellerService;

import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/sellers")
public class SellerController {

    private final SellerService sellerService;

    public SellerController(
            SellerService sellerService
    ) {
        this.sellerService = sellerService;
    }

    @PostMapping("/register")
    public ResponseEntity<SellerResponse> register(
            Authentication authentication,
            @Valid @RequestBody
            SellerRegistrationRequest request
    ) {

        SellerResponse response =
                sellerService.register(
                        authentication.getName(),
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @GetMapping("/me")
    public ResponseEntity<SellerResponse> myProfile(
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                sellerService.getMySellerProfile(
                        authentication.getName()
                )
        );
    }
}