package com.astra.order.service;

import com.astra.order.dto.StockEvent;
import com.astra.order.entity.CustomerOrder;
import com.astra.order.repository.CustomerOrderRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Locale;

@Service
public class OrderInventoryEventProcessor {

    private static final Logger log =
            LoggerFactory.getLogger(OrderInventoryEventProcessor.class);

    private final CustomerOrderRepository orderRepository;

    public OrderInventoryEventProcessor(
            CustomerOrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }

    @Transactional
    public void process(StockEvent event) {

        if (event.orderNumber() == null
                || event.orderNumber().isBlank()) {
            throw new IllegalArgumentException(
                    "Inventory event has no order number");
        }

        CustomerOrder order = orderRepository
                .findByOrderNumberForUpdate(event.orderNumber())
                .orElseThrow(() -> new IllegalStateException(
                        "Order not found: " + event.orderNumber()));

        String eventType = event.eventType() == null
                ? ""
                : event.eventType().trim().toUpperCase(Locale.ROOT);

        if ("STOCK_RESERVED".equals(eventType)
                || "RESERVED".equals(eventType)) {

            if ("RESERVED".equals(order.getStockStatus())) {
                log.info("Stock already reserved for order {}",
                        event.orderNumber());
                return;
            }

            if (!"PENDING".equals(order.getStockStatus())) {
                log.warn("Ignoring stock reservation for order {} with stock status {}",
                        event.orderNumber(), order.getStockStatus());
                return;
            }

            order.setStockStatus("RESERVED");

            if ("COD".equalsIgnoreCase(order.getPaymentMethod())) {
                order.setOrderStatus("CONFIRMED");
            } else if ("SUCCESS".equalsIgnoreCase(
                    order.getPaymentStatus())) {
                order.setOrderStatus("CONFIRMED");
            } else if ("FAILED".equalsIgnoreCase(
                    order.getPaymentStatus())) {
                order.setOrderStatus("PAYMENT_FAILED");
            } else {
                order.setOrderStatus("PENDING_PAYMENT");
            }

            log.info("Stock reserved for order {}. Payment status: {}",
                    event.orderNumber(), order.getPaymentStatus());

        } else if ("STOCK_RESERVATION_REJECTED".equals(eventType)
                || "RESERVATION_REJECTED".equals(eventType)
                || "REJECTED".equals(eventType)) {

            if ("REJECTED".equals(order.getStockStatus())) {
                log.info("Stock rejection already processed for order {}",
                        event.orderNumber());
                return;
            }

            if (!"PENDING".equals(order.getStockStatus())) {
                log.warn("Ignoring stock rejection for order {} with stock status {}",
                        event.orderNumber(), order.getStockStatus());
                return;
            }

            order.setStockStatus("REJECTED");
            order.setOrderStatus("STOCK_REJECTED");

            log.warn("Stock reservation rejected for order {}: {}",
                    event.orderNumber(), event.message());

        } else {
            throw new IllegalArgumentException(
                    "Unknown inventory event type: " + eventType);
        }

        orderRepository.save(order);
    }
}