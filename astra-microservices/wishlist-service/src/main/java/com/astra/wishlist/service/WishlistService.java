
package com.astra.wishlist.service;

import com.astra.wishlist.client.ProductClient;
import com.astra.wishlist.dto.*;
import com.astra.wishlist.entity.WishlistItem;
import com.astra.wishlist.repository.WishlistRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

@Service
public class WishlistService {

    private final WishlistRepository repository;
    private final ProductClient productClient;

    public WishlistService(
            WishlistRepository repository,
            ProductClient productClient) {
        this.repository = repository;
        this.productClient = productClient;
    }

    private String currentUserEmail() {
        Authentication authentication =
                SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null ||
                !authentication.isAuthenticated() ||
                authentication.getName() == null) {
            throw new IllegalStateException(
                    "Authenticated customer not found");
        }

        return authentication.getName()
                .trim()
                .toLowerCase(Locale.ROOT);
    }

    @Transactional(readOnly = true)
    public WishlistResponse getWishlist() {
        String email = currentUserEmail();

        List<WishlistItemResponse> items =
                repository.findAllByUserEmailOrderByCreatedAtDesc(email)
                        .stream()
                        .map(item -> {
                            ProductResponse product =
                                    productClient.getProduct(
                                            item.getProductId());

                            return new WishlistItemResponse(
                                    product.id(),
                                    product.name(),
                                    product.description(),
                                    product.category(),
                                    product.imageUrl(),
                                    product.price(),
                                    product.stockQuantity(),
                                    item.getCreatedAt()
                            );
                        })
                        .toList();

        return new WishlistResponse(items, items.size());
    }

    @Transactional
    public WishlistResponse addItem(AddWishlistRequest request) {
        String email = currentUserEmail();

        ProductResponse product =
                productClient.getProduct(request.productId());

        boolean alreadyExists =
                repository.existsByUserEmailAndProductId(
                        email, product.id());

        if (!alreadyExists) {
            WishlistItem item = new WishlistItem();
            item.setUserEmail(email);
            item.setProductId(product.id());
            repository.save(item);
        }

        return getWishlist();
    }

    @Transactional
    public void removeItem(Long productId) {
        String email = currentUserEmail();

        repository.deleteByUserEmailAndProductId(
                email, productId);
    }

    @Transactional
    public void clearWishlist() {
        repository.deleteAllByUserEmail(currentUserEmail());
    }
}
