package com.astra.backend.order;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@Service
public class RazorpayPaymentService {
    private final String keyId;
    private final String keySecret;
    private final RestClient client;

    public RazorpayPaymentService(
            @Value("${app.payment.razorpay-key-id:}") String keyId,
            @Value("${app.payment.razorpay-key-secret:}") String keySecret) {
        this.keyId = keyId == null ? "" : keyId.trim();
        this.keySecret = keySecret == null ? "" : keySecret.trim();
        this.client = RestClient.builder()
                .baseUrl("https://api.razorpay.com/v1")
                .build();
    }

    public String getKeyId() {
        requireConfigured();
        return keyId;
    }

    public String getPublicKeyOrEmpty() {
        return keyId.isBlank() || keySecret.isBlank() ? "" : keyId;
    }

    public String createOrder(BigDecimal amount, String receipt) {
        requireConfigured();
        if (amount == null || amount.signum() <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Online payment requires a positive order total");
        }
        long amountPaise;
        try {
            amountPaise = amount.setScale(2, RoundingMode.UNNECESSARY)
                    .movePointRight(2).longValueExact();
        } catch (ArithmeticException ex) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT, "Order total has invalid precision");
        }
        try {
            Map<?, ?> response = client.post().uri("/orders")
                    .headers(headers -> headers.setBasicAuth(keyId, keySecret))
                    .body(Map.of(
                            "amount", amountPaise,
                            "currency", "INR",
                            "receipt", receipt))
                    .retrieve().body(Map.class);
            Object id = response == null ? null : response.get("id");
            if (!(id instanceof String providerOrderId)
                    || !providerOrderId.matches("[A-Za-z0-9_-]{1,80}")) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "Payment provider returned an invalid order reference");
            }
            return providerOrderId;
        } catch (RestClientException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Unable to create an online payment. Please retry.");
        }
    }

    public boolean isCapturedPayment(
            String providerOrderId,
            String paymentId,
            String signature) {
        requireConfigured();
        if (!validReference(providerOrderId)
                || !validReference(paymentId)
                || signature == null
                || !signature.matches("[a-fA-F0-9]{64}")) {
            return false;
        }
        if (!MessageDigest.isEqual(
                expectedSignature(providerOrderId, paymentId)
                        .getBytes(StandardCharsets.US_ASCII),
                signature.toLowerCase(java.util.Locale.ROOT)
                        .getBytes(StandardCharsets.US_ASCII))) {
            return false;
        }
        try {
            Map<?, ?> payment = client.get()
                    .uri("/payments/{paymentId}", paymentId)
                    .headers(headers -> headers.setBasicAuth(keyId, keySecret))
                    .retrieve().body(Map.class);
            return payment != null
                    && providerOrderId.equals(payment.get("order_id"))
                    && "captured".equals(payment.get("status"));
        } catch (RestClientException ex) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "Unable to verify payment with the payment provider");
        }
    }

    String expectedSignature(String providerOrderId, String paymentId) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                    keySecret.getBytes(StandardCharsets.UTF_8),
                    "HmacSHA256"));
            return java.util.HexFormat.of().formatHex(mac.doFinal(
                    (providerOrderId + "|" + paymentId)
                            .getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to verify payment signature", ex);
        }
    }

    private boolean validReference(String value) {
        return value != null && value.matches("[A-Za-z0-9_-]{1,80}");
    }

    private void requireConfigured() {
        if (keyId.isBlank() || keySecret.isBlank()) {
            throw new ResponseStatusException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Online payment is not configured. Choose Cash on Delivery.");
        }
    }
}
