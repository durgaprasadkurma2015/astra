
package com.astra.inventory.dto;

import com.astra.inventory.entity.InventoryStock;

public record InventoryResponse(
    Long productId,
    int availableQuantity,
    int reservedQuantity
) {
    public static InventoryResponse from(InventoryStock stock) {
        return new InventoryResponse(
            stock.getProductId(),
            stock.getAvailableQuantity(),
            stock.getReservedQuantity()
        );
    }
}
