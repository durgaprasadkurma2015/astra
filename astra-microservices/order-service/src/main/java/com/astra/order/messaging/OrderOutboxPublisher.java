
package com.astra.order.messaging;

import com.astra.order.entity.OrderOutboxEvent;
import com.astra.order.repository.OrderOutboxRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class OrderOutboxPublisher {

    private static final Logger log =
            LoggerFactory.getLogger(OrderOutboxPublisher.class);

    private final OrderOutboxRepository repository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    public OrderOutboxPublisher(
            OrderOutboxRepository repository,
            KafkaTemplate<String, String> kafkaTemplate) {
        this.repository = repository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelay = 2000)
    public void publishPendingEvents() {
        List<OrderOutboxEvent> events =
                repository.findTop50ByEventStatusOrderByCreatedAtAsc(
                        "PENDING");

        for (OrderOutboxEvent event : events) {
            try {
                kafkaTemplate.send(
                        event.getTopic(),
                        event.getAggregateId(),
                        event.getPayload()
                ).get(10, TimeUnit.SECONDS);

                markPublished(event.getId());

                log.info("Published order event {} for {}",
                        event.getEventId(), event.getAggregateId());

            } catch (Exception ex) {
                markFailedAttempt(event.getId());

                log.error("Failed to publish order event {}",
                        event.getEventId(), ex);
            }
        }
    }

    @Transactional
    public void markPublished(Long id) {
        repository.findById(id).ifPresent(event -> {
            event.setEventStatus("PUBLISHED");
            event.setPublishedAt(LocalDateTime.now());
            repository.save(event);
        });
    }

    @Transactional
    public void markFailedAttempt(Long id) {
        repository.findById(id).ifPresent(event -> {
            event.setRetryCount(event.getRetryCount() + 1);
            repository.save(event);
        });
    }
}
