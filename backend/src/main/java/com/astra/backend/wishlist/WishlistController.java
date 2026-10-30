package com.astra.backend.wishlist;

import com.astra.backend.wishlist.dto.WishlistCheckResponse;
import com.astra.backend.wishlist.dto.WishlistResponse;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @GetMapping
    public List<WishlistResponse> getMyWishlist() {
        return wishlistService.getMyWishlist();
    }

    @PostMapping("/items/{productId}")
    public ResponseEntity<WishlistResponse> addToWishlist(
            @PathVariable Long productId) {

        WishlistResponse response =
            wishlistService.addToWishlist(productId);

        return ResponseEntity
            .status(HttpStatus.OK)
            .body(response);
    }

    @DeleteMapping("/items/{productId}")
    public ResponseEntity<Void> removeFromWishlist(
            @PathVariable Long productId) {

        wishlistService.removeFromWishlist(productId);

        return ResponseEntity.noContent().build();
    }

    @GetMapping("/items/{productId}/exists")
    public WishlistCheckResponse checkWishlist(
            @PathVariable Long productId) {

        return wishlistService.checkWishlist(productId);
    }
}