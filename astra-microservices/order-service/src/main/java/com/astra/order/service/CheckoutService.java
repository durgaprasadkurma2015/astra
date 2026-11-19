
package com.astra.order.service;

import com.astra.order.client.CartClient;
import com.astra.order.dto.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClientException;

import java.util.Locale;

@Service
public class CheckoutService {

    private static final Logger log =
            LoggerFactory.getLogger(CheckoutService.class);

    private final CartClient cartClient;
    private final OrderWriter orderWriter;

    public CheckoutService(
            CartClient cartClient,
            OrderWriter orderWriter) {
        this.cartClient = cartClient;
        this.orderWriter = orderWriter;
    }

    public OrderResponse checkout(
            String email,
            String idempotencyKey,
            CheckoutRequest request) {

        if (idempotencyKey == null
                || idempotencyKey.isBlank()
                || idempotencyKey.trim().length() > 100) {
            throw new IllegalArgumentException(
                    "A valid Idempotency-Key header is required");
        }

        if (request == null) {
            throw new IllegalArgumentException(
                    "Checkout request is required");
        }

        String key = idempotencyKey.trim();
        String requestHash = CheckoutFingerprint.create(request);

        // Idempotency lookup before fetching the cart.
        // A retry must return the original order even if the cart is empty.
        // The writer performs a second lookup inside its transaction.
        String normalizedEmail = email.trim().toLowerCase(Locale.ROOT);

     // First check whether this checkout was already processed.
     var existing = orderWriter.findExisting(
             normalizedEmail,
             key,
             requestHash);

     if (existing.isPresent()) {
         return existing.get();
     }

     // Only fetch the cart when this is a new checkout.
     var cart = cartClient.getCart();

     OrderResponse response = orderWriter.create(
             normalizedEmail,
             key,
             requestHash,
             request,
             cart);
        // Cart deletion is best-effort. Order persistence and its outbox
        // event must not be rolled back by a remote cart cleanup failure.
        for (CartItemResponse item : cart.items()) {
            try {
                cartClient.removeItem(item.productId());
            } catch (RestClientException | IllegalStateException ex) {
                log.warn("Could not remove product {} from cart after order {}",
                        item.productId(), response.orderNumber(), ex);
            }
        }

        return response;
    }
}
