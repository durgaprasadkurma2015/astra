
package com.astra.order.service;

import com.astra.order.dto.CheckoutRequest;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HexFormat;
import java.util.Locale;

public final class CheckoutFingerprint {

    private CheckoutFingerprint() {}

    public static String create(CheckoutRequest request) {
        String canonical = part(normalize(request.customerName()))
                + part(normalize(request.phoneNumber()))
                + part(normalize(request.shippingAddress()))
                + part(request.couponCode() == null
                    ? ""
                    : request.couponCode().trim().toUpperCase(Locale.ROOT));

        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(canonical.getBytes(StandardCharsets.UTF_8));

            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException(
                    "SHA-256 is not available", ex);
        }
    }

    private static String normalize(String value) {
        return value == null ? "" : value.trim();
    }

    private static String part(String value) {
        return value.length() + ":" + value;
    }
}
