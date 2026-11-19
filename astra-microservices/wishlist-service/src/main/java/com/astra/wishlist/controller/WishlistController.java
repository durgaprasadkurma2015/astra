
package com.astra.wishlist.controller;

import com.astra.wishlist.dto.AddWishlistRequest;
import com.astra.wishlist.dto.WishlistResponse;
import com.astra.wishlist.service.WishlistService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/wishlist")
public class WishlistController {

    private final WishlistService wishlistService;

    public WishlistController(WishlistService wishlistService) {
        this.wishlistService = wishlistService;
    }

    @GetMapping
    public WishlistResponse getWishlist() {
        return wishlistService.getWishlist();
    }

    @PostMapping("/items")
    @ResponseStatus(HttpStatus.CREATED)
    public WishlistResponse addItem(
            @Valid @RequestBody AddWishlistRequest request) {
        return wishlistService.addItem(request);
    }

    @DeleteMapping("/items/{productId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void removeItem(@PathVariable Long productId) {
        wishlistService.removeItem(productId);
    }

    @DeleteMapping
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void clearWishlist() {
        wishlistService.clearWishlist();
    }
}
