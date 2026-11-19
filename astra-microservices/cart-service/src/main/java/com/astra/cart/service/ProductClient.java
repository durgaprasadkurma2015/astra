package com.astra.cart.service;

import com.astra.cart.dto.ProductResponse;
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
public class ProductClient {

    private final RestClient restClient;
    private final String productServiceUrl;
    private final CurrentJwtToken currentJwtToken;

    public ProductClient(
    		@LoadBalanced
            RestClient.Builder builder,
            CurrentJwtToken currentJwtToken,
            @Value("${app.services.product-service:http://PRODUCT-SERVICE}")
            String productServiceUrl) {

        this.restClient = builder.build();
        this.currentJwtToken = currentJwtToken;
        this.productServiceUrl = productServiceUrl;
    }

    public ProductResponse getProduct(Long productId) {

        try {
            ProductResponse product = restClient.get()
                .uri(
                    productServiceUrl + "/api/v1/products/{id}",
                    productId
                )
                .headers(headers -> addAuthorization(headers))
                .retrieve()
                .body(ProductResponse.class);

            if (product == null ||
                    !Boolean.TRUE.equals(product.active())) {
                throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "Product not found or inactive"
                );
            }

            return product;

        } catch (HttpClientErrorException.NotFound ex) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Product not found"
            );
        } catch (RestClientException ex) {
            throw new ResponseStatusException(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Product Service is currently unavailable"
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