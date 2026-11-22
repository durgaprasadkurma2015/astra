
package com.astra.notification.messaging;

import com.astra.notification.entity.NotificationEvent;
import com.astra.notification.service.EmailNotificationService;
import com.astra.notification.service.NotificationDeliveryService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@ConditionalOnProperty(
    prefix = "app.notification.delivery",
    name = "enabled",
    havingValue = "true"
)
public class NotificationDeliveryScheduler {

    private static final Logger log =
        LoggerFactory.getLogger(NotificationDeliveryScheduler.class);

    private final NotificationDeliveryService deliveryService;
    private final EmailNotificationService emailService;

    public NotificationDeliveryScheduler(
            NotificationDeliveryService deliveryService,
            EmailNotificationService emailService) {
        this.deliveryService = deliveryService;
        this.emailService = emailService;
    }

    @Scheduled(
        fixedDelayString = "${app.notification.delivery.fixed-delay:10000}"
    )
    public void deliverPendingNotifications() {

        List<Long> dueIds = deliveryService.findDueNotifications();

        for (Long id : dueIds) {
            try {
                Optional<NotificationEvent> claimed =
                    deliveryService.claimNotification(id);

                if (claimed.isEmpty()) {
                    continue;
                }

                NotificationEvent notification = claimed.get();

                if (notification.getRecipientEmail() == null
                        || notification.getRecipientEmail().isBlank()) {

                    deliveryService.markPermanentFailure(
                        id, "Recipient email is missing"
                    );
                    continue;
                }

                try {
                    emailService.send(notification);
                    deliveryService.markSent(id);

                    log.info(
                        "Notification {} sent successfully",
                        notification.getEventId()
                    );

                } catch (Exception exception) {

                    deliveryService.markFailed(
                        id, exception.getMessage()
                    );

                    log.error(
                        "Email delivery failed for notification {}",
                        notification.getEventId(),
                        exception
                    );
                }

            } catch (Exception exception) {
                log.error(
                    "Unable to process notification ID {}",
                    id,
                    exception
                );
            }
        }
    }
}
