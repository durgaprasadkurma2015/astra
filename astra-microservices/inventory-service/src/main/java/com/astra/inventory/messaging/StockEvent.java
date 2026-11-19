
package com.astra.inventory.messaging;

import java.util.List;

public record StockEvent(
    String eventId,
    String eventType,
    String orderNumber,
    String message,
    List<Item> items
) {
    public record Item(Long productId, int quantity) {}
}
