
package com.astra.notification.messaging;

import com.astra.notification.service.NotificationService;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@Component
public class NotificationEventConsumer {

    private final NotificationService notificationService;

    public NotificationEventConsumer(
            NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @KafkaListener(
        topics = {
            "order.created",
            "payment.success",
            "payment.failed"
        },
        groupId = "notification-service"
    )
    public void consume(ConsumerRecord<String, String> record) {

        notificationService.processEvent(
            record.topic(),
            record.value()
        );
    }
}
