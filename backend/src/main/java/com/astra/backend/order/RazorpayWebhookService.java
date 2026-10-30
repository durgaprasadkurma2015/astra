package com.astra.backend.order;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RazorpayWebhookService {
    private final String webhookSecret;
    private final ObjectMapper objectMapper;
    private final OrderService orderService;

    public RazorpayWebhookService(
            @Value("${app.payment.razorpay-webhook-secret:}") String webhookSecret,
            ObjectMapper objectMapper,
            OrderService orderService) {
        this.webhookSecret = webhookSecret == null ? "" : webhookSecret.trim();
        this.objectMapper = objectMapper;
        this.orderService = orderService;
    }

    public void accept(String rawBody, String signature) {
        if (webhookSecret.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Payment webhooks are not configured");
        }
        if (rawBody == null || rawBody.isBlank()
                || signature == null || !signature.matches("[a-fA-F0-9]{64}")
                || !MessageDigest.isEqual(
                        signatureFor(rawBody).getBytes(StandardCharsets.US_ASCII),
                        signature.toLowerCase(java.util.Locale.ROOT)
                                .getBytes(StandardCharsets.US_ASCII))) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED, "Invalid payment webhook signature");
        }
        try {
            JsonNode event = objectMapper.readTree(rawBody);
            if (!"payment.captured".equals(event.path("event").asText())) return;
            JsonNode payment = event.path("payload").path("payment").path("entity");
            String orderId = payment.path("order_id").asText("");
            String paymentId = payment.path("id").asText("");
            if (!orderId.matches("[A-Za-z0-9_-]{1,80}")
                    || !paymentId.matches("[A-Za-z0-9_-]{1,80}")
                    || !"captured".equals(payment.path("status").asText())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "Invalid captured payment event");
            }
            orderService.recordCapturedPayment(orderId, paymentId);
        } catch (ResponseStatusException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Invalid payment webhook payload");
        }
    }

    private String signatureFor(String body) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                    webhookSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return HexFormat.of().formatHex(mac.doFinal(
                    body.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to verify webhook signature", ex);
        }
    }
}
