package com.astra.order.messaging;

import com.astra.order.dto.StockEvent;
import com.astra.order.service.OrderInventoryEventProcessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class InventoryEventConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(InventoryEventConsumer.class);

    private final ObjectMapper objectMapper;
    private final OrderInventoryEventProcessor processor;

    public InventoryEventConsumer(
            ObjectMapper objectMapper,
            OrderInventoryEventProcessor processor) {
        this.objectMapper = objectMapper;
        this.processor = processor;
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

        log.info("Received inventory event: {} for order {}",
                event.eventType(), event.orderNumber());

        processor.process(event);
    }
}