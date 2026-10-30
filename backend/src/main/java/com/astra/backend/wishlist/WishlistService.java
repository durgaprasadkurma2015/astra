package com.astra.backend.wishlist;

import com.astra.backend.product.Product;
import com.astra.backend.product.ProductRepository;
import com.astra.backend.user.AppUser;
import com.astra.backend.user.AppUserRepository;
import com.astra.backend.wishlist.dto.WishlistCheckResponse;
import com.astra.backend.wishlist.dto.WishlistResponse;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
public class WishlistService {

    private final WishlistRepository wishlistRepository;
    private final ProductRepository productRepository;
    private final AppUserRepository appUserRepository;

    public WishlistService(
            WishlistRepository wishlistRepository,
            ProductRepository productRepository,
            AppUserRepository appUserRepository) {

        this.wishlistRepository = wishlistRepository;
        this.productRepository = productRepository;
        this.appUserRepository = appUserRepository;
    }

    private String currentEmail() {
        Authentication authentication =
            SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null
                || !authentication.isAuthenticated()
                || authentication.getName() == null
                || authentication.getName().isBlank()
                || "anonymousUser".equals(authentication.getName())) {

            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Please log in to access your wishlist"
            );
        }

        return authentication.getName()
            .trim()
            .toLowerCase(Locale.ROOT);
    }

    private AppUser currentUser() {
        String email = currentEmail();

        return appUserRepository.findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "User associated with this session was not found"
            ));
    }

    @Transactional(readOnly = true)
    public List<WishlistResponse> getMyWishlist() {

        AppUser user = currentUser();

        return wishlistRepository
            .findByUser_IdOrderByCreatedAtDescIdDesc(user.getId())
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional
    public WishlistResponse addToWishlist(Long productId) {

        AppUser user = currentUser();

        Product product = productRepository.findById(productId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Product not found"
            ));

        // Adding an existing item again is safe and idempotent.
        var existing = wishlistRepository
            .findByUser_IdAndProduct_Id(
                user.getId(),
                productId
            );

        if (existing.isPresent()) {
            return toResponse(existing.get());
        }

        if (!Boolean.TRUE.equals(product.getActive())) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Product not found or is no longer available"
            );
        }

        Wishlist wishlist = new Wishlist();
        wishlist.setUser(user);
        wishlist.setProduct(product);

        Wishlist saved = wishlistRepository.save(wishlist);

        return toResponse(saved);
    }

    @Transactional
    public void removeFromWishlist(Long productId) {

        AppUser user = currentUser();

        Wishlist wishlist = wishlistRepository
            .findByUser_IdAndProduct_Id(
                user.getId(),
                productId
            )
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Product not found in your wishlist"
            ));

        wishlistRepository.delete(wishlist);
    }

    @Transactional(readOnly = true)
    public WishlistCheckResponse checkWishlist(Long productId) {

        AppUser user = currentUser();

        boolean inWishlist = wishlistRepository
            .existsByUser_IdAndProduct_Id(
                user.getId(),
                productId
            );

        return new WishlistCheckResponse(
            productId,
            inWishlist
        );
    }

    private WishlistResponse toResponse(Wishlist wishlist) {

        Product product = wishlist.getProduct();

        return new WishlistResponse(
            product.getId(),
            product.getName(),
            product.getDescription(),
            product.getCategory(),
            product.getImageUrl(),
            product.getPrice(),
            product.getDiscountedPrice(),
            product.getStockQuantity(),
            product.getActive(),
            wishlist.getCreatedAt()
        );
    }
}