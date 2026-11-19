
package com.astra.inventory.messaging;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class StockEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public StockEventPublisher(
        KafkaTemplate<String, String> kafkaTemplate,
        ObjectMapper objectMapper
    ) {
        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
    }

    public void publish(String topic, String eventType,
                        String orderNumber, String message,
                        java.util.List<StockEvent.Item> items) {

        try {
            StockEvent event = new StockEvent(
                UUID.randomUUID().toString(),
                eventType,
                orderNumber,
                message,
                items
            );

            String json = objectMapper.writeValueAsString(event);

            kafkaTemplate.send(topic, orderNumber, json);
        } catch (JsonProcessingException ex) {
            throw new IllegalStateException(
                "Could not serialize stock event", ex
            );
        }
    }
}
