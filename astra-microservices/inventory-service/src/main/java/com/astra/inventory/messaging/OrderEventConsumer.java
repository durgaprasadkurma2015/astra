
package com.astra.inventory.messaging;

import com.astra.inventory.dto.ReservationRequest;
import com.astra.inventory.dto.ReservationResponse;
import com.astra.inventory.entity.ReservationStatus;
import com.astra.inventory.service.InventoryService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class OrderEventConsumer {

    private final ObjectMapper objectMapper;
    private final InventoryService inventoryService;
    private final StockEventPublisher eventPublisher;

    public OrderEventConsumer(
        ObjectMapper objectMapper,
        InventoryService inventoryService,
        StockEventPublisher eventPublisher
    ) {
        this.objectMapper = objectMapper;
        this.inventoryService = inventoryService;
        this.eventPublisher = eventPublisher;
    }

    @KafkaListener(
        topics = "order.created",
        groupId = "inventory-service"
    )
    public void handleOrderCreated(String message) throws Exception {

        OrderCreatedEvent event =
            objectMapper.readValue(message, OrderCreatedEvent.class);

        ReservationRequest request = new ReservationRequest(
            event.orderNumber(),
            event.items().stream()
                .map(item -> new ReservationRequest.ReservationItemRequest(
                    item.productId(), item.quantity()
                ))
                .toList()
        );

        ReservationResponse response = inventoryService.reserve(request);

        boolean reserved =
            response.status() == ReservationStatus.RESERVED;

        List<StockEvent.Item> items = response.items().stream()
            .map(item -> new StockEvent.Item(
                item.productId(), item.quantity()
            ))
            .toList();

        eventPublisher.publish(
            reserved ? "stock.reserved" : "stock.reservation-rejected",
            reserved ? "StockReserved" : "StockReservationRejected",
            response.orderNumber(),
            reserved ? "Stock reserved for order" :
                "Stock reservation rejected",
            items
        );
    }
}
