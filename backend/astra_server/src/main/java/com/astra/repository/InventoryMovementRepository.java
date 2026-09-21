package com.astra.repository;

import com.astra.entity.InventoryMovement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryMovementRepository
        extends JpaRepository<InventoryMovement, Long> {

    Page<InventoryMovement> findByProductIdOrderByCreatedAtDesc(
            Long productId,
            Pageable pageable
    );

    Page<InventoryMovement> findByOrderIdOrderByCreatedAtDesc(
            Long orderId,
            Pageable pageable
    );
}