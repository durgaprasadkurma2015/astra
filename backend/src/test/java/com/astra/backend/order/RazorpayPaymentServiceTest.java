package com.astra.backend.order;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

class RazorpayPaymentServiceTest {
    @Test
    void paymentCreationIsDisabledWithoutMerchantCredentials() {
        RazorpayPaymentService service = new RazorpayPaymentService("", "");
        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class,
                () -> service.createOrder(new BigDecimal("10.00"), "AST-ORDER"));
        assertEquals(HttpStatus.SERVICE_UNAVAILABLE, exception.getStatusCode());
    }

    @Test
    void rejectsInvalidSignatureWithoutCallingProvider() {
        RazorpayPaymentService service = new RazorpayPaymentService(
                "rzp_test_key", "test_secret");
        assertFalse(service.isCapturedPayment(
                "order_123", "pay_123", "0".repeat(64)));
    }

    @Test
    void rejectsMalformedProviderReferences() {
        RazorpayPaymentService service = new RazorpayPaymentService(
                "rzp_test_key", "test_secret");
        assertFalse(service.isCapturedPayment(
                "order/123", "pay_123", "0".repeat(64)));
    }
}
