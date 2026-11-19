package com.astra.backend.kafka;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class AstraKafkaConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(AstraKafkaConsumer.class);

    private final ObjectMapper objectMapper;

    public AstraKafkaConsumer(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @KafkaListener(
            topics = "${app.kafka.topic:astra-events}",
            groupId = "${spring.kafka.consumer.group-id:astra-event-consumers}"
    )
    public void consume(String message) {

        try {
            JsonNode event = objectMapper.readTree(message);

            String eventType =
                    event.path("eventType").asText("UNKNOWN");

            String eventId =
                    event.path("eventId").asText();

            log.info(
                    "ASTRA KAFKA EVENT RECEIVED | type={} | eventId={} | data={}",
                    eventType,
                    eventId,
                    event.path("data")
            );

        } catch (Exception ex) {

            log.error(
                    "Unable to process Astra Kafka message: {}",
                    message,
                    ex
            );

            throw new IllegalArgumentException(
                    "Invalid Astra Kafka event payload",
                    ex
            );
        }
    }
}