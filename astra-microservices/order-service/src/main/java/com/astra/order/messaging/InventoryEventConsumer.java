
package com.astra.order.messaging;

import com.astra.order.dto.StockEvent;
import com.astra.order.entity.CustomerOrder;
import com.astra.order.repository.CustomerOrderRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class InventoryEventConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(InventoryEventConsumer.class);

    private final ObjectMapper objectMapper;
    private final CustomerOrderRepository orderRepository;

    public InventoryEventConsumer(
            ObjectMapper objectMapper,
            CustomerOrderRepository orderRepository) {
        this.objectMapper = objectMapper;
        this.orderRepository = orderRepository;
    }

    @KafkaListener(
            topics = {
                    "stock.reserved",
                    "stock.reservation-rejected"
            },
            groupId = "order-service"
    )
    public void consume(String payload) throws Exception {
        StockEvent event =
                objectMapper.readValue(payload, StockEvent.class);

        if (event.orderNumber() == null
                || event.orderNumber().isBlank()) {
            log.warn("Ignoring inventory event without order number");
            return;
        }

        applyInventoryResult(event);
    }

    @Transactional
    public void applyInventoryResult(StockEvent event) {
        CustomerOrder order = orderRepository
                .findByOrderNumberForUpdate(event.orderNumber())
                .orElse(null);

        if (order == null) {
            log.warn("Order {} not found for inventory event",
                    event.orderNumber());
            return;
        }

        if (!"PENDING_STOCK".equals(order.getOrderStatus())) {
            log.info("Ignoring inventory event {} for order {} in status {}",
                    event.eventId(),
                    event.orderNumber(),
                    order.getOrderStatus());
            return;
        }

        if ("STOCK_RESERVED".equalsIgnoreCase(event.eventType())
                || "RESERVED".equalsIgnoreCase(event.eventType())) {
            order.setOrderStatus("CONFIRMED");
            log.info("Stock reserved for order {}", event.orderNumber());

        } else if ("STOCK_RESERVATION_REJECTED"
                .equalsIgnoreCase(event.eventType())
                || "RESERVATION_REJECTED"
                .equalsIgnoreCase(event.eventType())
                || "REJECTED".equalsIgnoreCase(event.eventType())) {
            order.setOrderStatus("STOCK_REJECTED");
            log.warn("Stock reservation rejected for order {}: {}",
                    event.orderNumber(), event.message());

        } else {
            log.warn("Unknown inventory event type: {}",
                    event.eventType());
            return;
        }

        orderRepository.save(order);
    }
}
