
package com.astra.payment.service;

import com.astra.payment.entity.PaymentEvent;
import com.astra.payment.repository.PaymentEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
public class PaymentOutboxPublisher {

    private static final Logger log =
        LoggerFactory.getLogger(PaymentOutboxPublisher.class);

    private final PaymentEventRepository eventRepository;
    private final KafkaTemplate<String, String> kafkaTemplate;

    @Value("${app.outbox.batch-size:50}")
    private int batchSize;

    public PaymentOutboxPublisher(
            PaymentEventRepository eventRepository,
            KafkaTemplate<String, String> kafkaTemplate) {
        this.eventRepository = eventRepository;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Scheduled(fixedDelayString = "${app.outbox.poll-interval-ms:2000}")
    public void publishPendingEvents() {

        List<PaymentEvent> events =
            eventRepository.findTop50ByEventStatusOrderByCreatedAtAsc(
                "PENDING"
            );

        for (PaymentEvent event : events) {
            try {
                kafkaTemplate.send(
                    event.getTopic(),
                    event.getEventId(),
                    event.getPayload()
                ).get(10, TimeUnit.SECONDS);

                event.setEventStatus("PUBLISHED");
                event.setPublishedAt(LocalDateTime.now());
                eventRepository.save(event);

                log.info(
                    "Published payment event: eventId={}, topic={}",
                    event.getEventId(),
                    event.getTopic()
                );

            } catch (Exception exception) {

                event.setRetryCount(event.getRetryCount() + 1);
                eventRepository.save(event);

                log.error(
                    "Payment event publishing failed: eventId={}, retryCount={}",
                    event.getEventId(),
                    event.getRetryCount(),
                    exception
                );
            }
        }
    }
}
