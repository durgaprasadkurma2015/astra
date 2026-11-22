
package com.astra.payment.gateway;

import com.astra.payment.entity.Payment;

public interface PaymentGateway {

    MockPaymentResult processPayment(Payment payment);
}
