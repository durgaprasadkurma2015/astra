package com.astra.cart.controller;

import com.astra.cart.dto.AddCartItemRequest;
import com.astra.cart.dto.CartResponse;
import com.astra.cart.dto.UpdateCartItemRequest;
import com.astra.cart.service.CartService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;

import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

    private final CartService cartService;

    public CartController(
            CartService cartService
    ) {

        this.cartService =
                cartService;
    }

    @GetMapping
    public ResponseEntity<CartResponse>
    getCart(
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                cartService.getCart(
                        getEmail(authentication)
                )
        );
    }

    @PostMapping("/items")
    public ResponseEntity<CartResponse>
    addItem(

            Authentication authentication,

            @Valid
            @RequestBody AddCartItemRequest request

    ) {

        return ResponseEntity.ok(
                cartService.addItem(
                        getEmail(authentication),
                        request
                )
        );
    }

    @PutMapping("/items/{itemId}")
    public ResponseEntity<CartResponse>
    updateItem(

            Authentication authentication,

            @PathVariable Long itemId,

            @Valid
            @RequestBody UpdateCartItemRequest request

    ) {

        return ResponseEntity.ok(
                cartService.updateItem(
                        getEmail(authentication),
                        itemId,
                        request
                )
        );
    }

    @DeleteMapping("/items/{itemId}")
    public ResponseEntity<CartResponse>
    removeItem(

            Authentication authentication,

            @PathVariable Long itemId

    ) {

        return ResponseEntity.ok(
                cartService.removeItem(
                        getEmail(authentication),
                        itemId
                )
        );
    }

    @DeleteMapping
    public ResponseEntity<CartResponse>
    clearCart(
            Authentication authentication
    ) {

        return ResponseEntity.ok(
                cartService.clearCart(
                        getEmail(authentication)
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