package com.astra.backend.product;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InventoryStockMovementRepository
        extends JpaRepository<InventoryStockMovement, Long> {
    List<InventoryStockMovement> findByProductIdOrderByCreatedAtDesc(Long productId);
    Page<InventoryStockMovement> findByProductIdOrderByCreatedAtDesc(Long productId, Pageable pageable);
}
