
package com.astra.notification.service;

import com.astra.notification.dto.NotificationResponse;
import com.astra.notification.entity.NotificationEvent;
import com.astra.notification.repository.NotificationRepository;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;

@Service
public class NotificationService {

    private final NotificationRepository repository;
    private final ObjectMapper objectMapper;

    public NotificationService(
            NotificationRepository repository,
            ObjectMapper objectMapper) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void processEvent(String topic, String payload) {

        try {
            JsonNode json = objectMapper.readTree(payload);

            String eventId = firstText(json, "eventId", "event_id");
            if (eventId == null || eventId.isBlank()) {
                eventId = createPayloadHash(topic + ":" + payload);
            }

            if (repository.existsByEventId(eventId)) {
                return;
            }

            String eventType = firstText(json, "eventType", "event_type");
            if (eventType == null || eventType.isBlank()) {
                eventType = topic.toUpperCase(Locale.ROOT)
                    .replace('.', '_');
            }

            String recipient = firstText(
                json, "customerEmail", "userEmail", "recipientEmail", "email"
            );

            String orderNumber = firstText(json, "orderNumber", "order_number");

            NotificationEvent notification = new NotificationEvent();
            notification.setEventId(eventId);
            notification.setEventType(eventType);
            notification.setRecipientEmail(recipient);
            notification.setOrderNumber(orderNumber);
            notification.setSubject(buildSubject(topic, orderNumber));
            notification.setMessage(buildMessage(topic, orderNumber));
            notification.setDeliveryStatus("PENDING");

            repository.save(notification);

        } catch (Exception exception) {
            throw new IllegalArgumentException(
                "Unable to process notification event from topic " + topic,
                exception
            );
        }
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getAllNotifications() {
        return repository.findAllByOrderByCreatedAtDesc()
            .stream()
            .map(NotificationResponse::from)
            .toList();
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getPendingNotifications() {
        return repository.findByDeliveryStatusOrderByCreatedAtDesc("PENDING")
            .stream()
            .map(NotificationResponse::from)
            .toList();
    }

    private String firstText(JsonNode json, String... fields) {
        for (String field : fields) {
            JsonNode value = json.get(field);
            if (value != null && !value.isNull() && value.isValueNode()) {
                String text = value.asText();
                if (!text.isBlank()) {
                    return text;
                }
            }
        }
        return null;
    }

    private String buildSubject(String topic, String orderNumber) {
        String order = orderNumber == null ? "" : " - " + orderNumber;

        return switch (topic) {
            case "order.created" -> "Astra order received" + order;
            case "payment.success" -> "Astra payment successful" + order;
            case "payment.failed" -> "Astra payment failed" + order;
            default -> "Astra order update" + order;
        };
    }

    private String buildMessage(String topic, String orderNumber) {
        String order = orderNumber == null ? "your order" : "order " + orderNumber;

        return switch (topic) {
            case "order.created" ->
                "We have received " + order + ". Thank you for shopping with Astra.";
            case "payment.success" ->
                "Payment for " + order + " was successful.";
            case "payment.failed" ->
                "Payment for " + order + " failed. Please check your payment details.";
            default ->
                "There is an update for " + order + ".";
        };
    }

    private String createPayloadHash(String value) throws Exception {
        byte[] hash = MessageDigest.getInstance("SHA-256")
            .digest(value.getBytes(StandardCharsets.UTF_8));
        return "payload-" + HexFormat.of().formatHex(hash);
    }
}
