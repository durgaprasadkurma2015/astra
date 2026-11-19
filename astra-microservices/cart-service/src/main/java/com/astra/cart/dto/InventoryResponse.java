
package com.astra.cart.dto;

public record InventoryResponse(
    Long productId,
    Integer availableQuantity,
    Integer reservedQuantity
) {
}
