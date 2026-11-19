package com.astra.backend.event.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionalEventListener;
import org.springframework.transaction.event.TransactionPhase;

import com.astra.backend.event.model.OrderPlacedEvent;
import com.astra.backend.event.model.OrderCancelledEvent;
import com.astra.backend.event.model.PaymentCompletedEvent;
import com.astra.backend.event.model.OrderStatusChangedEvent;

import com.astra.backend.kafka.AstraKafkaProducer;

@Component
public class AstraOrderEventListener {

    private static final Logger log =
            LoggerFactory.getLogger(AstraOrderEventListener.class);

    private final AstraKafkaProducer kafkaProducer;

    public AstraOrderEventListener(AstraKafkaProducer kafkaProducer) {
        this.kafkaProducer = kafkaProducer;
    }

    @Async("taskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderPlaced(OrderPlacedEvent event) {

        log.info(
                "ASTRA ORDER PLACED | order={} | customer={} | amount={}",
                event.orderNumber(),
                event.customerEmail(),
                event.totalAmount()
        );

        kafkaProducer.publish(
                "ORDER_PLACED",
                String.valueOf(event.orderId()),
                event
        );
    }

    @Async("taskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCancelled(OrderCancelledEvent event) {

        log.info(
                "ASTRA ORDER CANCELLED | order={}",
                event.orderNumber()
        );

        kafkaProducer.publish(
                "ORDER_CANCELLED",
                String.valueOf(event.orderId()),
                event
        );
    }

    @Async("taskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentCompleted(PaymentCompletedEvent event) {

        log.info(
                "ASTRA PAYMENT COMPLETED | order={} | paymentId={}",
                event.orderNumber(),
                event.paymentId()
        );

        kafkaProducer.publish(
                "PAYMENT_COMPLETED",
                String.valueOf(event.orderId()),
                event
        );
    }

    @Async("taskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderStatusChanged(OrderStatusChangedEvent event) {

        log.info(
                "ASTRA ORDER STATUS CHANGED | order={} | {} -> {}",
                event.orderNumber(),
                event.previousStatus(),
                event.newStatus()
        );

        kafkaProducer.publish(
                "ORDER_STATUS_CHANGED",
                String.valueOf(event.orderId()),
                event
        );
    }
}