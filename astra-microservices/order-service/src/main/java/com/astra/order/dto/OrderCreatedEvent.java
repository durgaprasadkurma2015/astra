package com.astra.order.dto;

import java.util.List;

public record OrderCreatedEvent(
        String orderNumber,
        List<Item> items
) {
    public record Item(
            Long productId,
            Integer quantity
    ) {}
}