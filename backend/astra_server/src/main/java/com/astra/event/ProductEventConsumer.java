package com.astra.event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.kafka.annotation.DltHandler;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.RetryableTopic;

import org.springframework.retry.annotation.Backoff;

import org.springframework.stereotype.Component;

@Component
public class ProductEventConsumer {

    private static final Logger log =
            LoggerFactory.getLogger(ProductEventConsumer.class);

    @RetryableTopic(
            attempts = "4",
            backoff = @Backoff(
                    delay = 1000,
                    multiplier = 2.0,
                    maxDelay = 10000
            )
    )
    @KafkaListener(
            topics = "${astra.kafka.product-topic:astra.product.events}",
            groupId = "${astra.kafka.product-group:astra-search-indexer}",
            autoStartup = "${ASTRA_KAFKA_LISTENER_AUTO_STARTUP:false}"
    )
    public void consume(ProductEvent event) {

        log.info("Product event received: {}", event);

        // Search-indexing/recommendation consumers
        // can be extracted into separate services later.
    }

    @DltHandler
    public void deadLetter(ProductEvent event) {

        log.error(
                "Product event moved to DLT after retries: {}",
                event
        );
    }
}
