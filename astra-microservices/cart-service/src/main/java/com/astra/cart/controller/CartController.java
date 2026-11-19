
package com.astra.cart.controller;

import com.astra.cart.dto.AddToCartRequest;
import com.astra.cart.dto.CartResponse;
import com.astra.cart.dto.UpdateCartQuantityRequest;
import com.astra.cart.service.CartService;

import jakarta.validation.Valid;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @GetMapping
    public CartResponse getCart() {
        return cartService.getCart();
    }

    @PostMapping("/items")
    public CartResponse addItem(
            @Valid @RequestBody AddToCartRequest request) {

        return cartService.addItem(request);
    }

    @PutMapping("/items/{productId}")
    public CartResponse updateQuantity(
            @PathVariable Long productId,
            @Valid @RequestBody UpdateCartQuantityRequest request) {

        return cartService.updateQuantity(
            productId,
            request.quantity()
        );
    }

    @DeleteMapping("/items/{productId}")
    public CartResponse removeItem(
            @PathVariable Long productId) {

        return cartService.removeItem(productId);
    }
}
