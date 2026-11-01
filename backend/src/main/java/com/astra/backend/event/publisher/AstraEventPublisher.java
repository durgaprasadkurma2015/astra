package com.astra.backend.event.publisher;

import java.math.BigDecimal;
import java.time.Instant;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

import com.astra.backend.event.model.OrderPlacedEvent;
import com.astra.backend.event.model.OrderCancelledEvent;
import com.astra.backend.event.model.PaymentCompletedEvent;
import com.astra.backend.event.model.OrderStatusChangedEvent;
import com.astra.backend.event.publisher.AstraEventPublisher;

@Component
public class AstraEventPublisher {

    private final ApplicationEventPublisher publisher;

    public AstraEventPublisher(ApplicationEventPublisher publisher) {
        this.publisher = publisher;
    }

    public void orderPlaced(
            Long orderId,
            String orderNumber,
            String customerEmail,
            BigDecimal totalAmount,
            String paymentMethod) {

        publisher.publishEvent(new OrderPlacedEvent(
                orderId,
                orderNumber,
                customerEmail,
                totalAmount,
                paymentMethod,
                Instant.now()
        ));
    }

    public void orderCancelled(
            Long orderId,
            String orderNumber,
            String customerEmail) {

        publisher.publishEvent(new OrderCancelledEvent(
                orderId,
                orderNumber,
                customerEmail,
                Instant.now()
        ));
    }

    public void paymentCompleted(
            Long orderId,
            String orderNumber,
            String customerEmail,
            String paymentId,
            BigDecimal amount) {

        publisher.publishEvent(new PaymentCompletedEvent(
                orderId,
                orderNumber,
                customerEmail,
                paymentId,
                amount,
                Instant.now()
        ));
    }

    public void orderStatusChanged(
            Long orderId,
            String orderNumber,
            String previousStatus,
            String newStatus) {

        publisher.publishEvent(new OrderStatusChangedEvent(
                orderId,
                orderNumber,
                previousStatus,
                newStatus,
                Instant.now()
        ));
    }
}