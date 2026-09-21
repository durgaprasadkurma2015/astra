package com.astra.wishlist.controller;

import com.astra.cart.dto.CartResponse;
import com.astra.dto.AddWishlistItemRequest;
import com.astra.wishlist.dto.WishlistResponse;
import com.astra.wishlist.service.WishlistService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(
            WishlistService wishlistService
    ) {

        this.wishlistService =
                wishlistService;
    }

    @GetMapping
    public ResponseEntity<WishlistResponse>
    getWishlist(
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                wishlistService.getWishlist(
                        getEmail(authentication)
                )
        );
    }

    @PostMapping("/items")
    public ResponseEntity<WishlistResponse>
    addItem(

            Authentication authentication,

            @Valid
            @RequestBody AddWishlistItemRequest request

    ) {

        return ResponseEntity.ok(
                wishlistService.addItem(
                        getEmail(authentication),
                        request
                )
        );
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<WishlistResponse>
    removeItem(

            Authentication authentication,

            @PathVariable Long itemId

    ) {

        return ResponseEntity.ok(
                wishlistService.removeItem(
                        getEmail(authentication),
                        itemId
                )
        );
    }

    @DeleteMapping("/products/{productId}")
    public ResponseEntity<WishlistResponse>
    removeProduct(

            Authentication authentication,

            @PathVariable Long productId

    ) {

        return ResponseEntity.ok(
                wishlistService.removeProduct(
                        getEmail(authentication),
                        productId
                )
        );
    }

    @GetMapping("/products/{productId}/exists")
    public ResponseEntity<Boolean>
    exists(

            Authentication authentication,

            @PathVariable Long productId

    ) {

        return ResponseEntity.ok(
                wishlistService.exists(
                        getEmail(authentication),
                        productId
                )
        );
    }

    @DeleteMapping
    public ResponseEntity<WishlistResponse>
    clear(
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                wishlistService.clear(
                        getEmail(authentication)
                )
        );
    }

    @PostMapping("/items/{itemId}/cart")
    public ResponseEntity<CartResponse>
    moveToCart(

            Authentication authentication,

            @PathVariable Long itemId

    ) {

        return ResponseEntity.ok(
                wishlistService.moveToCart(
                        getEmail(authentication),
                        itemId
                )
        );
    }

    private String getEmail(
            Authentication authentication
    ) {

        if (authentication == null ||
                !authentication.isAuthenticated()) {

            throw new IllegalArgumentException(
                    "Authentication is required"
            );
        }

        return authentication.getName();
    }
}