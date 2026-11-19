package com.astra.cart.service;

import com.astra.cart.dto.InventoryResponse;
import com.astra.cart.security.CurrentJwtToken;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

@Component
public class InventoryClient {

    private final RestClient restClient;
    private final String inventoryServiceUrl;
    private final CurrentJwtToken currentJwtToken;

    public InventoryClient(
    		@LoadBalanced 
            RestClient.Builder builder,
            CurrentJwtToken currentJwtToken,
            @Value("${app.services.inventory-service:http://INVENTORY-SERVICE}")
            String inventoryServiceUrl) {

        this.restClient = builder.build();
        this.currentJwtToken = currentJwtToken;
        this.inventoryServiceUrl = inventoryServiceUrl;
    }

    public int getAvailableQuantity(Long productId) {

        try {
            InventoryResponse inventory = restClient.get()
                .uri(
                    inventoryServiceUrl
                        + "/api/v1/inventory/stock/{productId}",
                    productId
                )
                .headers(headers -> addAuthorization(headers))
                .retrieve()
                .body(InventoryResponse.class);

            if (inventory == null ||
                    inventory.availableQuantity() == null) {
                return 0;
            }

            return inventory.availableQuantity();

        } catch (HttpClientErrorException.NotFound ex) {
            return 0;
        } catch (RestClientException ex) {
            throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Inventory Service is currently unavailable"
            );
        }
    }

    private void addAuthorization(HttpHeaders headers) {
        String token = currentJwtToken.getToken();

        if (token != null) {
            headers.setBearerAuth(token);
        }
    }
}