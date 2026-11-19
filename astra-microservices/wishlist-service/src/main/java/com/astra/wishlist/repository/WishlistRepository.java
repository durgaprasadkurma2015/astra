
package com.astra.wishlist.repository;

import com.astra.wishlist.entity.WishlistItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WishlistRepository
        extends JpaRepository<WishlistItem, Long> {

    List<WishlistItem> findAllByUserEmailOrderByCreatedAtDesc(
            String userEmail);

    Optional<WishlistItem> findByUserEmailAndProductId(
            String userEmail, Long productId);

    boolean existsByUserEmailAndProductId(
            String userEmail, Long productId);

    void deleteByUserEmailAndProductId(
            String userEmail, Long productId);

    void deleteAllByUserEmail(String userEmail);
}
