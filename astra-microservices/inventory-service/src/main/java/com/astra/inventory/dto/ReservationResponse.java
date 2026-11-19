
package com.astra.inventory.dto;

import com.astra.inventory.entity.StockReservation;
import com.astra.inventory.entity.ReservationStatus;
import java.time.LocalDateTime;
import java.util.List;

public record ReservationResponse(
    String orderNumber,
    ReservationStatus status,
    LocalDateTime expiresAt,
    List<Item> items
) {
    public record Item(Long productId, int quantity) {}

    public static ReservationResponse from(StockReservation reservation) {
        return new ReservationResponse(
            reservation.getOrderNumber(),
            reservation.getStatus(),
            reservation.getExpiresAt(),
            reservation.getItems().stream()
                .map(i -> new Item(i.getProductId(), i.getQuantity()))
                .toList()
        );
    }
}
