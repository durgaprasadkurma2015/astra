package com.astra.repository;

import com.astra.entity.CartItem;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartItemRepository
        extends JpaRepository<CartItem, Long> {

    Optional<CartItem> findByIdAndCartId(
            Long itemId,
            Long cartId
    );

    Optional<CartItem> findByCartIdAndProductId(
            Long cartId,
            Long productId
    );

    long countByCartId(Long cartId);

    void deleteByCartId(Long cartId);
}