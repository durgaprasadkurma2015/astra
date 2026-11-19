package com.astra.backend.kafka;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class AstraKafkaProducer {

    private static final Logger log =
            LoggerFactory.getLogger(AstraKafkaProducer.class);

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;
    private final String topic;

    public AstraKafkaProducer(
            KafkaTemplate<String, String> kafkaTemplate,
            ObjectMapper objectMapper,
            @Value("${app.kafka.topic:astra-events}") String topic) {

        this.kafkaTemplate = kafkaTemplate;
        this.objectMapper = objectMapper;
        this.topic = topic;
    }

    public void publish(
            String eventType,
            String messageKey,
            Object eventPayload) {

        try {
            Map<String, Object> event = new LinkedHashMap<>();

            event.put("eventId", UUID.randomUUID().toString());
            event.put("eventType", eventType);
            event.put("occurredAt", Instant.now().toString());
            event.put("data", eventPayload);

            String json = objectMapper.writeValueAsString(event);

            kafkaTemplate.send(topic, messageKey, json)
                    .whenComplete((result, error) -> {

                        if (error != null) {
                            log.error(
                                    "Kafka publish failed | type={} | key={}",
                                    eventType,
                                    messageKey,
                                    error
                            );
                        } else {
                            log.info(
                                    "Kafka event published | type={} | topic={} | partition={} | offset={}",
                                    eventType,
                                    topic,
                                    result.getRecordMetadata().partition(),
                                    result.getRecordMetadata().offset()
                            );
                        }
                    });

        } catch (JsonProcessingException ex) {
            log.error("Kafka event serialization failed", ex);
        }
    }
}