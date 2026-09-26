package com.astra.event;

import com.astra.config.RabbitConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;

@Component
@ConditionalOnProperty(name = "astra.rabbit.enabled", havingValue = "true")
public class RabbitProductEventPublisher {
    private static final Logger log = LoggerFactory.getLogger(RabbitProductEventPublisher.class);
    private final RabbitTemplate rabbitTemplate;

    public RabbitProductEventPublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    public void publish(ProductEvent event) {
        try {
            rabbitTemplate.convertAndSend(
                    RabbitConfig.EXCHANGE,
                    "product." + event.type().toLowerCase(),
                    event
            );
        } catch (Exception ex) {
            log.warn("RabbitMQ unavailable; product event was not published: {}", event, ex);
        }
    }
}
