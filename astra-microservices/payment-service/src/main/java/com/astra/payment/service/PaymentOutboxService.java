
package com.astra.payment.service;

import com.astra.payment.entity.Payment;
import com.astra.payment.entity.PaymentEvent;
import com.astra.payment.entity.PaymentStatus;
import com.astra.payment.repository.PaymentEventRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Service
public class PaymentOutboxService {

    private final PaymentEventRepository eventRepository;
    private final ObjectMapper objectMapper;

    public PaymentOutboxService(
            PaymentEventRepository eventRepository,
            ObjectMapper objectMapper) {
        this.eventRepository = eventRepository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void recordPaymentResult(Payment payment) {

        PaymentStatus status = payment.getPaymentStatus();

        String eventType;
        String topic;

        if (status == PaymentStatus.SUCCESS) {
            eventType = "PAYMENT_SUCCESS";
            topic = "payment.success";
        } else if (status == PaymentStatus.FAILED) {
            eventType = "PAYMENT_FAILED";
            topic = "payment.failed";
        } else {
            // Do not publish a final payment event for PENDING.
            return;
        }

        Map<String, Object> payload = Map.of(
            "eventId", UUID.randomUUID().toString(),
            "eventType", eventType,
            "paymentReference", payment.getPaymentReference(),
            "orderNumber", payment.getOrderNumber(),
            "customerEmail", payment.getCustomerEmail(),
            "amount", payment.getAmount(),
            "currency", payment.getCurrency(),
            "paymentStatus", status.name(),
            "occurredAt", LocalDateTime.now().toString()
        );

        try {
            PaymentEvent event = new PaymentEvent();

            event.setEventId((String) payload.get("eventId"));
            event.setPaymentId(payment.getId());
            event.setEventType(eventType);
            event.setTopic(topic);
            event.setPayload(objectMapper.writeValueAsString(payload));
            event.setEventStatus("PENDING");
            event.setRetryCount(0);

            eventRepository.save(event);

        } catch (JsonProcessingException exception) {
            throw new IllegalStateException(
                "Unable to serialize payment outbox event",
                exception
            );
        }
    }
}
