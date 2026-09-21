package com.astra.dto;

import com.astra.enums.InventoryMovementType;

import java.time.LocalDateTime;

public record InventoryMovementResponse(

        Long id,

        Long productId,

        Long orderId,

        InventoryMovementType movementType,

        Integer quantity,

        Integer quantityBefore,

        Integer quantityAfter,

        String reason,

        LocalDateTime createdAt
) {
}