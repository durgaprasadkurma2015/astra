
package com.astra.payment.util;

import com.astra.payment.dto.PaymentRequest;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;

public final class PaymentFingerprint {

    private PaymentFingerprint() {
    }

    public static String create(PaymentRequest request) {
        String canonical = String.join("|",
            request.orderNumber().trim(),
            request.amount().stripTrailingZeros().toPlainString(),
            request.currency().trim().toUpperCase(),
            request.paymentMethod().name()
        );

        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");

            byte[] hash = digest.digest(
                canonical.getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(hash);

        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(
                "Unable to calculate payment fingerprint", ex
            );
        }
    }
}
