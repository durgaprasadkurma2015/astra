package com.astra.order.service;

import com.astra.order.dto.PaymentEventPayload;
import com.astra.order.entity.CustomerOrder;
import com.astra.order.entity.ProcessedPaymentEvent;
import com.astra.order.repository.CustomerOrderRepository;
import com.astra.order.repository.ProcessedPaymentEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Locale;

@Service
public class OrderPaymentEventProcessor {

    private static final Logger log =
            LoggerFactory.getLogger(OrderPaymentEventProcessor.class);

    private final CustomerOrderRepository orderRepository;
    private final ProcessedPaymentEventRepository eventRepository;

    public OrderPaymentEventProcessor(
            CustomerOrderRepository orderRepository,
            ProcessedPaymentEventRepository eventRepository) {
        this.orderRepository = orderRepository;
        this.eventRepository = eventRepository;
    }

    @Transactional
    public void process(PaymentEventPayload event) {

        validateEvent(event);

        // Idempotency: ignore events already committed to the database.
        if (eventRepository.existsByEventId(event.eventId())) {
            log.info("Payment event {} already processed",
                    event.eventId());
            return;
        }

        CustomerOrder order = orderRepository
                .findByOrderNumberForUpdate(event.orderNumber())
                .orElseThrow(() -> new IllegalStateException(
                        "Order not found: " + event.orderNumber()));

        // Verify that the payment belongs to this order's customer.
        if (!order.getUserEmail().equalsIgnoreCase(
                event.customerEmail().trim())) {
            throw new IllegalStateException(
                    "Payment customer does not match order customer");
        }

        // Verify payment amount against the stored order amount.
        if (order.getTotalAmount().compareTo(event.amount()) != 0) {
            throw new IllegalStateException(
                    "Payment amount does not match order amount");
        }

        if (!"INR".equalsIgnoreCase(event.currency())) {
            throw new IllegalStateException(
                    "Unsupported payment currency: " + event.currency());
        }

        String eventType = event.eventType()
                .trim()
                .toUpperCase(Locale.ROOT);

        if ("PAYMENT_SUCCESS".equals(eventType)
                && "SUCCESS".equalsIgnoreCase(event.paymentStatus())) {

            if ("FAILED".equalsIgnoreCase(order.getPaymentStatus())) {
                throw new IllegalStateException(
                        "Order already has a failed payment");
            }

            if ("SUCCESS".equalsIgnoreCase(order.getPaymentStatus())) {
                log.info("Payment already successful for order {}",
                        event.orderNumber());
            } else {
                order.setPaymentStatus("SUCCESS");

                if ("RESERVED".equalsIgnoreCase(order.getStockStatus())) {
                    order.setOrderStatus("CONFIRMED");
                } else if ("REJECTED".equalsIgnoreCase(
                        order.getStockStatus())) {
                    order.setOrderStatus("STOCK_REJECTED");
                }

                log.info("Payment successful for order {}",
                        event.orderNumber());
            }

        } else if ("PAYMENT_FAILED".equals(eventType)
                && "FAILED".equalsIgnoreCase(event.paymentStatus())) {

            if ("SUCCESS".equalsIgnoreCase(order.getPaymentStatus())) {
                throw new IllegalStateException(
                        "Order already has a successful payment");
            }

            order.setPaymentStatus("FAILED");
            order.setOrderStatus("PAYMENT_FAILED");

            log.warn("Payment failed for order {}. Reason: {}",
                    event.orderNumber(), event.paymentReference());

        } else {
            throw new IllegalArgumentException(
                    "Unsupported payment event: " + event.eventType()
                            + " / " + event.paymentStatus());
        }

        orderRepository.save(order);

        // Persist the event in the same transaction as the order update.
        ProcessedPaymentEvent processed = new ProcessedPaymentEvent();
        processed.setEventId(event.eventId());
        processed.setEventType(event.eventType());
        processed.setOrderNumber(event.orderNumber());

        eventRepository.save(processed);
    }

    private void validateEvent(PaymentEventPayload event) {

        if (event == null
                || event.eventId() == null
                || event.eventId().isBlank()
                || event.eventType() == null
                || event.eventType().isBlank()
                || event.orderNumber() == null
                || event.orderNumber().isBlank()
                || event.customerEmail() == null
                || event.customerEmail().isBlank()
                || event.paymentReference() == null
                || event.paymentReference().isBlank()
                || event.amount() == null
                || event.amount().compareTo(BigDecimal.ZERO) <= 0
                || event.currency() == null
                || event.paymentStatus() == null) {

            throw new IllegalArgumentException(
                    "Invalid payment event payload");
        }
    }
}