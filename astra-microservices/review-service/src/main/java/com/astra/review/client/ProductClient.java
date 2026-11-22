
package com.astra.review.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

@Component
public class ProductClient {

    private final RestClient restClient;

    public ProductClient(
        @Qualifier("loadBalancedRestClientBuilder")
        RestClient.Builder builder,
        @Value("${app.services.product-service}") String productServiceUrl
    ) {
        this.restClient = builder.baseUrl(productServiceUrl).build();
    }

    public ProductInfo getActiveProduct(Long productId, String token) {
        try {
            ProductInfo product = restClient.get()
                .uri("/api/v1/products/{id}", productId)
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .retrieve()
                .body(ProductInfo.class);

            if (product == null || !Boolean.TRUE.equals(product.active())) {
                throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Product not found"
                );
            }

            return product;

        } catch (RestClientResponseException ex) {
            if (ex.getStatusCode().value() == 404) {
                throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND, "Product not found"
                );
            }

            throw new ResponseStatusException(
                HttpStatus.BAD_GATEWAY,
                "Product Service request failed"
            );
        }
    }

    public record ProductInfo(
        Long id,
        String name,
        Boolean active
    ) {}
}
