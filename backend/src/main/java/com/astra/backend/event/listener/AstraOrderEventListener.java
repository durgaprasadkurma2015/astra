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

@Component
public class AstraOrderEventListener {

    private static final Logger log =
            LoggerFactory.getLogger(AstraOrderEventListener.class);

    @Async("taskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderPlaced(OrderPlacedEvent event) {

        log.info(
                "ASTRA ORDER PLACED | order={} | customer={} | amount={} | method={}",
                event.orderNumber(),
                event.customerEmail(),
                event.totalAmount(),
                event.paymentMethod()
        );
    }

    @Async("taskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCancelled(OrderCancelledEvent event) {

        log.info(
                "ASTRA ORDER CANCELLED | order={} | customer={}",
                event.orderNumber(),
                event.customerEmail()
        );
    }

    @Async("taskExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handlePaymentCompleted(PaymentCompletedEvent event) {

        log.info(
                "ASTRA PAYMENT COMPLETED | order={} | paymentId={} | amount={}",
                event.orderNumber(),
                event.paymentId(),
                event.amount()
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
    }
}