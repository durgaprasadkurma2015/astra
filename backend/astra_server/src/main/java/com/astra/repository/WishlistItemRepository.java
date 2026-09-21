package com.astra.repository;

import com.astra.entity.WishlistItem;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WishlistItemRepository
        extends JpaRepository<WishlistItem, Long> {

    Optional<WishlistItem> findByIdAndWishlistId(
            Long itemId,
            Long wishlistId
    );

    Optional<WishlistItem> findByWishlistIdAndProductId(
            Long wishlistId,
            Long productId
    );

    boolean existsByWishlistIdAndProductId(
            Long wishlistId,
            Long productId
    );

    void deleteByWishlistIdAndProductId(
            Long wishlistId,
            Long productId
    );

    void deleteByWishlistId(
            Long wishlistId
    );

    long countByWishlistId(
            Long wishlistId
    );
}