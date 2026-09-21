package com.astra.inventory.dto;

public record InventoryResponse(

        Long productId,

        String productName,

        Integer availableQuantity,

        Integer reservedQuantity,

        Integer totalQuantity
) {
}