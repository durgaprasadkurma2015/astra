
package com.astra.review.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Component
public class OrderClient {

    private final RestClient restClient;

    public OrderClient(
        @Qualifier("loadBalancedRestClientBuilder")
        RestClient.Builder builder,
        @Value("${app.services.order-service}") String orderServiceUrl
    ) {
        this.restClient = builder.baseUrl(orderServiceUrl).build();
    }

    public boolean hasDeliveredPurchase(Long productId, String token) {
        try {
            List<OrderInfo> orders = restClient.get()
                .uri("/api/v1/orders/my-orders")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .body(new ParameterizedTypeReference<List<OrderInfo>>() {});

            if (orders == null) {
                return false;
            }

            return orders.stream()
                .filter(order -> "DELIVERED".equalsIgnoreCase(
                    order.orderStatus()
                ))
                .filter(order -> order.items() != null)
                .anyMatch(order -> order.items().stream()
                    .anyMatch(item -> productId.equals(item.productId()))
                );

        } catch (RestClientException ex) {
            throw new ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                "Unable to verify purchase with Order Service"
            );
        }
    }

    private record OrderInfo(
        String orderNumber,
        String orderStatus,
        List<OrderItemInfo> items
    ) {}

    private record OrderItemInfo(
        Long productId,
        String productName,
        Integer quantity
    ) {}
}
