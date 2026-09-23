package com.astra.repository;

import com.astra.entity.Seller;
import com.astra.enums.SellerStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SellerRepository
        extends JpaRepository<Seller, Long> {

    Optional<Seller> findByUserId(Long userId);

    Optional<Seller> findByStoreSlug(String storeSlug);

    boolean existsByStoreSlug(String storeSlug);

    long countByStatus(SellerStatus status);

    Page<Seller> findByStatus(
            SellerStatus status,
            Pageable pageable
    );
}
