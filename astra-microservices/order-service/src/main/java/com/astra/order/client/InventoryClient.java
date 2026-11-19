
package com.astra.order.client;

import com.astra.order.security.CurrentJwtToken;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class InventoryClient {

    private final RestClient restClient;
    private final CurrentJwtToken currentJwtToken;

    public InventoryClient(
            @Qualifier("loadBalancedRestClientBuilder")
            RestClient.Builder builder,
            CurrentJwtToken currentJwtToken) {

        this.restClient = builder
                .baseUrl("http://INVENTORY-SERVICE")
                .build();

        this.currentJwtToken = currentJwtToken;
    }

    public void releaseReservation(String orderNumber) {
        restClient.post()
                .uri("/api/v1/inventory/reservations/{orderNumber}/release",
                        orderNumber)
                .headers(headers -> headers.setBearerAuth(
                        currentJwtToken.getToken()))
                .retrieve()
                .toBodilessEntity();
    }
}
