
package com.astra.inventory.repository;

import com.astra.inventory.entity.StockReservation;
import org.springframework.data.jpa.repository.*;
import java.util.Optional;

public interface StockReservationRepository
        extends JpaRepository<StockReservation, Long> {

    @EntityGraph(attributePaths = "items")
    Optional<StockReservation> findByOrderNumber(String orderNumber);
}
