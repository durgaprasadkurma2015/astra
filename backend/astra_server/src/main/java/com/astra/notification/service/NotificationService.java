package com.astra.notification.service;

import com.astra.dto.NotificationCountResponse;
import com.astra.entity.Notification;
import com.astra.entity.User;
import com.astra.enums.NotificationChannel;
import com.astra.enums.NotificationStatus;
import com.astra.enums.NotificationType;
import com.astra.integration.EmailNotificationProvider;
import com.astra.integration.InAppNotificationProvider;
import com.astra.integration.NotificationProvider;
import com.astra.integration.SmsNotificationProvider;
import com.astra.notification.dto.NotificationResponse;
import com.astra.repository.NotificationRepository;
import com.astra.repository.UserRepository;

import jakarta.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    private final EmailNotificationProvider emailProvider;
    private final SmsNotificationProvider smsProvider;
    private final InAppNotificationProvider inAppProvider;

    public NotificationService(
            NotificationRepository notificationRepository,
            UserRepository userRepository,
            EmailNotificationProvider emailProvider,
            SmsNotificationProvider smsProvider,
            InAppNotificationProvider inAppProvider) {

        this.notificationRepository = notificationRepository;
        this.userRepository = userRepository;
        this.emailProvider = emailProvider;
        this.smsProvider = smsProvider;
        this.inAppProvider = inAppProvider;
    }

    @Transactional
    public NotificationResponse send(
            Long userId,
            NotificationType type,
            NotificationChannel channel,
            String subject,
            String message,
            String referenceType,
            Long referenceId) {

        User user =
                userRepository.findById(userId)
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "User not found"
                                )
                        );

        Notification notification =
                new Notification();

        notification.setUser(user);
        notification.setType(type);
        notification.setChannel(channel);
        notification.setStatus(
                NotificationStatus.PENDING
        );

        notification.setSubject(subject);
        notification.setMessage(message);

        notification.setRecipient(
                resolveRecipient(
                        user,
                        channel
                )
        );

        notification.setReferenceType(
                referenceType
        );

        notification.setReferenceId(
                referenceId
        );

        notification =
                notificationRepository.save(notification);

        try {

            getProvider(channel).send(notification);

            notification.setStatus(
                    NotificationStatus.SENT
            );

            notification.setSentAt(
                    LocalDateTime.now()
            );

            notification.setFailureReason(null);

        } catch (Exception ex) {

            notification.setStatus(
                    NotificationStatus.FAILED
            );

            notification.setRetryCount(
                    notification.getRetryCount() + 1
            );

            notification.setFailureReason(
                    ex.getMessage()
            );
        }

        notification =
                notificationRepository.save(notification);

        return toResponse(notification);
    }

    @Transactional
    public Page<NotificationResponse> getMyNotifications(
            String email,
            Pageable pageable) {

        User user = getUser(email);

        return notificationRepository
                .findByUserIdOrderByCreatedAtDesc(
                        user.getId(),
                        pageable
                )
                .map(this::toResponse);
    }

    @Transactional
    public Page<NotificationResponse> getMyUnreadNotifications(
            String email,
            Pageable pageable) {

        User user = getUser(email);

        return notificationRepository
                .findByUserIdAndStatusOrderByCreatedAtDesc(
                        user.getId(),
                        NotificationStatus.SENT,
                        pageable
                )
                .map(this::toResponse);
    }

    @Transactional
    public NotificationCountResponse getUnreadCount(
            String email) {

        User user = getUser(email);

        long count =
                notificationRepository
                        .countByUserIdAndStatus(
                                user.getId(),
                                NotificationStatus.SENT
                        );

        return new NotificationCountResponse(count);
    }

    @Transactional
    public NotificationResponse markAsRead(
            Long notificationId,
            String email) {

        User user = getUser(email);

        Notification notification =
                notificationRepository
                        .findByIdAndUserId(
                                notificationId,
                                user.getId()
                        )
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Notification not found"
                                )
                        );

        notification.setStatus(
                NotificationStatus.READ
        );

        notification.setReadAt(
                LocalDateTime.now()
        );

        notification =
                notificationRepository.save(notification);

        return toResponse(notification);
    }

    @Transactional
    public void markAllAsRead(
            String email) {

        User user = getUser(email);

        Page<Notification> notifications =
                notificationRepository
                        .findByUserIdAndStatusOrderByCreatedAtDesc(
                                user.getId(),
                                NotificationStatus.SENT,
                                Pageable.unpaged()
                        );

        LocalDateTime now = LocalDateTime.now();

        for (Notification notification
                : notifications.getContent()) {

            notification.setStatus(
                    NotificationStatus.READ
            );

            notification.setReadAt(now);
        }

        notificationRepository.saveAll(
                notifications.getContent()
        );
    }

    /*
     * Business notifications
     */

    @Transactional
    public void sendOrderCreated(
            User user,
            Long orderId,
            String orderNumber) {

        send(
                user.getId(),
                NotificationType.ORDER_CREATED,
                NotificationChannel.IN_APP,
                "Order Created",
                "Your Astra order "
                        + orderNumber
                        + " has been created.",
                "ORDER",
                orderId
        );

        send(
                user.getId(),
                NotificationType.ORDER_CREATED,
                NotificationChannel.EMAIL,
                "Astra Order Created",
                "Your order "
                        + orderNumber
                        + " has been created successfully.",
                "ORDER",
                orderId
        );
    }

    @Transactional
    public void sendPaymentSuccess(
            User user,
            Long orderId,
            String orderNumber) {

        send(
                user.getId(),
                NotificationType.PAYMENT_SUCCESS,
                NotificationChannel.IN_APP,
                "Payment Successful",
                "Payment for order "
                        + orderNumber
                        + " was successful.",
                "ORDER",
                orderId
        );

        send(
                user.getId(),
                NotificationType.PAYMENT_SUCCESS,
                NotificationChannel.EMAIL,
                "Astra Payment Successful",
                "Payment for your order "
                        + orderNumber
                        + " was successful.",
                "ORDER",
                orderId
        );
    }

    @Transactional
    public void sendPaymentFailed(
            User user,
            Long orderId,
            String orderNumber) {

        send(
                user.getId(),
                NotificationType.PAYMENT_FAILED,
                NotificationChannel.IN_APP,
                "Payment Failed",
                "Payment for order "
                        + orderNumber
                        + " failed.",
                "ORDER",
                orderId
        );
    }

    @Transactional
    public void sendPaymentRefunded(
            User user,
            Long orderId,
            String orderNumber) {

        send(
                user.getId(),
                NotificationType.PAYMENT_REFUNDED,
                NotificationChannel.IN_APP,
                "Payment Refunded",
                "Your payment for order "
                        + orderNumber
                        + " has been refunded.",
                "ORDER",
                orderId
        );

        send(
                user.getId(),
                NotificationType.PAYMENT_REFUNDED,
                NotificationChannel.EMAIL,
                "Astra Payment Refunded",
                "Your payment for order "
                        + orderNumber
                        + " has been refunded.",
                "ORDER",
                orderId
        );
    }

    @Transactional
    public void sendShipmentCreated(
            User user,
            Long shipmentId,
            String trackingNumber) {

        send(
                user.getId(),
                NotificationType.SHIPMENT_CREATED,
                NotificationChannel.IN_APP,
                "Shipment Created",
                "Your shipment has been created. Tracking number: "
                        + trackingNumber,
                "SHIPMENT",
                shipmentId
        );

        send(
                user.getId(),
                NotificationType.SHIPMENT_CREATED,
                NotificationChannel.EMAIL,
                "Astra Shipment Created",
                "Your shipment has been created. Tracking number: "
                        + trackingNumber,
                "SHIPMENT",
                shipmentId
        );
    }

    @Transactional
    public void sendShipmentStatus(
            User user,
            Long shipmentId,
            NotificationType type,
            String message) {

        send(
                user.getId(),
                type,
                NotificationChannel.IN_APP,
                "Shipment Update",
                message,
                "SHIPMENT",
                shipmentId
        );

        send(
                user.getId(),
                type,
                NotificationChannel.EMAIL,
                "Astra Shipment Update",
                message,
                "SHIPMENT",
                shipmentId
        );
    }

    private NotificationProvider getProvider(
            NotificationChannel channel) {

        return switch (channel) {

            case EMAIL -> emailProvider;

            case SMS -> smsProvider;

            case IN_APP -> inAppProvider;
        };
    }

    private String resolveRecipient(
            User user,
            NotificationChannel channel) {

        return switch (channel) {

            case EMAIL -> user.getEmail();

            case SMS -> user.getPhone();

            case IN_APP -> String.valueOf(
                    user.getId()
            );
        };
    }

    private User getUser(String email) {

        return userRepository
//                .findByEmail(email)
        		.findByEmailIgnoreCase(email)
                .orElseThrow(
                        () -> new IllegalArgumentException(
                                "User not found"
                        )
                );
    }

    private NotificationResponse toResponse(
            Notification notification) {

        return new NotificationResponse(

                notification.getId(),

                notification.getType(),

                notification.getChannel(),

                notification.getStatus(),

                notification.getSubject(),

                notification.getMessage(),

                notification.getRecipient(),

                notification.getReferenceType(),

                notification.getReferenceId(),

                notification.getRetryCount(),

                notification.getSentAt(),

                notification.getReadAt(),

                notification.getCreatedAt()
        );
    }
}