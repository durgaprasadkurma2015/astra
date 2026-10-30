package com.astra.backend.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class RazorpayWebhookServiceTest {
    private final OrderService orders = org.mockito.Mockito.mock(OrderService.class);
    private final RazorpayWebhookService service = new RazorpayWebhookService(
            "webhook_secret", new ObjectMapper(), orders);

    @Test
    void processesOnlySignedCapturedPayments() throws Exception {
        String body = """
                {"event":"payment.captured","payload":{"payment":{"entity":{"order_id":"order_1","id":"pay_1","status":"captured"}}}}
                """;
        service.accept(body, signature(body));
        verify(orders).recordCapturedPayment("order_1", "pay_1");
    }

    @Test
    void rejectsInvalidWebhookSignatures() {
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.accept("{}", "0".repeat(64)));
        assertEquals(HttpStatus.UNAUTHORIZED, exception.getStatusCode());
    }

    private String signature(String body) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(
                "webhook_secret".getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        return HexFormat.of().formatHex(mac.doFinal(
                body.getBytes(StandardCharsets.UTF_8)));
    }
}
