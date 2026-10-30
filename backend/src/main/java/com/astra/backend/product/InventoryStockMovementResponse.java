package com.astra.backend.product;

import java.time.LocalDateTime;

public record InventoryStockMovementResponse(
        Long id,
        Long productId,
        String productName,
        String productSku,
        String actorEmail,
        int quantityDelta,
        int stockAfter,
        String reason,
        LocalDateTime createdAt) {

    public static InventoryStockMovementResponse from(InventoryStockMovement movement) {
        Product product = movement.getProduct();
        return new InventoryStockMovementResponse(
                movement.getId(), product.getId(), product.getName(),
                product.getSku(), movement.getActorEmail(),
                movement.getQuantityDelta(), movement.getStockAfter(),
                movement.getReason(), movement.getCreatedAt());
    }
}
