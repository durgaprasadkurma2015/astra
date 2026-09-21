package com.astra.repository;

import com.astra.entity.Shipment;
import com.astra.enums.ShipmentStatus;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ShipmentRepository
        extends JpaRepository<Shipment, Long> {

    Optional<Shipment> findByOrderId(Long orderId);

    Optional<Shipment> findByTrackingNumber(
            String trackingNumber
    );

    Optional<Shipment> findByIdAndUserId(
            Long id,
            Long userId
    );

    List<Shipment> findByUserIdOrderByCreatedAtDesc(
            Long userId
    );

    List<Shipment> findByStatusOrderByCreatedAtAsc(
            ShipmentStatus status
    );

    boolean existsByOrderId(Long orderId);

    boolean existsByTrackingNumber(String trackingNumber);
}