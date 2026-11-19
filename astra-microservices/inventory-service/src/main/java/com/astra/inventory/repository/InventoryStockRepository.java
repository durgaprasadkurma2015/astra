
package com.astra.inventory.repository;

import com.astra.inventory.entity.InventoryStock;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.*;
import org.springframework.data.repository.query.Param;
import java.util.*;

public interface InventoryStockRepository
        extends JpaRepository<InventoryStock, Long> {

    Optional<InventoryStock> findByProductId(Long productId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
        SELECT s FROM InventoryStock s
        WHERE s.productId IN :productIds
        ORDER BY s.productId
    """)
    List<InventoryStock> lockStocks(
        @Param("productIds") Collection<Long> productIds
    );
}
