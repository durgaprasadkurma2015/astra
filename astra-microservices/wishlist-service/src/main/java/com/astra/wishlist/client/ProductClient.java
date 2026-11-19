
package com.astra.wishlist.client;

import com.astra.wishlist.dto.ProductResponse;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

@Component
public class ProductClient {

    private final RestClient restClient;

    public ProductClient(
            @Qualifier("loadBalancedRestClientBuilder")
            RestClient.Builder builder) {

        this.restClient = builder
                .baseUrl("http://PRODUCT-SERVICE")
                .build();
    }

    public ProductResponse getProduct(Long productId) {
        try {
            ProductResponse product = restClient.get()
                    .uri("/api/v1/products/{id}", productId)
                    .retrieve()
                    .body(ProductResponse.class);

            if (product == null) {
                throw new IllegalStateException(
                        "Product service returned an empty response");
            }

            if (!Boolean.TRUE.equals(product.active())) {
                throw new IllegalArgumentException(
                        "Product is not active");
            }

            return product;

        } catch (HttpClientErrorException.NotFound ex) {
            throw new IllegalArgumentException(
                    "Product not found: " + productId);
        } catch (RestClientException ex) {
            throw new IllegalStateException(
                    "Product service is currently unavailable", ex);
        }
    }
}
