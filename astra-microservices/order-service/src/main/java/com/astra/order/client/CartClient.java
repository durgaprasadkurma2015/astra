
package com.astra.order.client;

import com.astra.order.dto.CartResponse;
import com.astra.order.security.CurrentJwtToken;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class CartClient {

    private final RestClient restClient;
    private final CurrentJwtToken currentJwtToken;

    public CartClient(
            @Qualifier("loadBalancedRestClientBuilder")
            RestClient.Builder builder,
            CurrentJwtToken currentJwtToken) {

        this.restClient = builder
                .baseUrl("http://CART-SERVICE")
                .build();

        this.currentJwtToken = currentJwtToken;
    }

    public CartResponse getCart() {
        try {
            CartResponse response = restClient.get()
                    .uri("/api/v1/cart")
                    .headers(headers -> headers.setBearerAuth(
                            currentJwtToken.getToken()))
                    .retrieve()
                    .body(CartResponse.class);

            if (response == null || response.items() == null) {
                throw new IllegalStateException(
                        "Cart Service returned an invalid response");
            }

            return response;

        } catch (RestClientException ex) {
            throw new IllegalStateException(
                    "Unable to retrieve your cart from Cart Service", ex);
        }
    }

    public void removeItem(Long productId) {
        restClient.delete()
                .uri("/api/v1/cart/items/{productId}", productId)
                .headers(headers -> headers.setBearerAuth(
                        currentJwtToken.getToken()))
                .retrieve()
                .toBodilessEntity();
    }
}
