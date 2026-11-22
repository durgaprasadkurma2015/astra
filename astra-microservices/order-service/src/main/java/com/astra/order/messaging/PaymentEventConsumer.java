package com.astra.order.messaging;

import com.astra.order.dto.PaymentEventPayload;
import com.astra.order.service.OrderPaymentEventProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class PaymentEventConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(PaymentEventConsumer.class);

    private final ObjectMapper objectMapper;
    private final OrderPaymentEventProcessor processor;

    public PaymentEventConsumer(
            ObjectMapper objectMapper,
            OrderPaymentEventProcessor processor) {
        this.objectMapper = objectMapper;
        this.processor = processor;
    }

    @KafkaListener(
            topics = {
                    "payment.success",
                    "payment.failed"
            },
            groupId = "order-payment-service"
    )
    public void consume(String payload) throws Exception {

        PaymentEventPayload event =
                objectMapper.readValue(
                        payload, PaymentEventPayload.class);

        log.info("Received payment event: {} for order {}",
                event.eventType(), event.orderNumber());

        processor.process(event);
    }
}