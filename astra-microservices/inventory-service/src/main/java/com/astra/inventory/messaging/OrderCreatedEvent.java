
package com.astra.inventory.messaging;

import java.util.List;

public record OrderCreatedEvent(
    String orderNumber,
    List<Item> items
) {
    public record Item(Long productId, int quantity) {}
}
