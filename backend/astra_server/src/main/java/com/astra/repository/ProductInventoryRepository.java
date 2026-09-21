package com.astra.repository;

import com.astra.entity.ProductInventory;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface ProductInventoryRepository
        extends JpaRepository<ProductInventory, Long> {

    Optional<ProductInventory> findByProductId(Long productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT pi
            FROM ProductInventory pi
            WHERE pi.product.id = :productId
            """)
    Optional<ProductInventory> findWithLockByProductId(
            @Param("productId") Long productId
    );
}