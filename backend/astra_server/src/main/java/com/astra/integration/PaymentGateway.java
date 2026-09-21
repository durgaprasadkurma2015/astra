package com.astra.integration;

import com.astra.entity.PaymentTransaction;

public interface PaymentGateway {

    PaymentGatewayResult createPayment(
            PaymentTransaction transaction
    );

    PaymentGatewayResult verifyPayment(
            PaymentTransaction transaction
    );

    PaymentGatewayResult refundPayment(
            PaymentTransaction transaction
    );
}