package com.astra.backend.product;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

class ProductReviewServiceTest {
    private final ProductReviewRepository reviews =
            org.mockito.Mockito.mock(ProductReviewRepository.class);
    private final ProductRepository products =
            org.mockito.Mockito.mock(ProductRepository.class);
    private final com.astra.backend.order.CustomerOrderRepository orders =
            org.mockito.Mockito.mock(com.astra.backend.order.CustomerOrderRepository.class);
    private final com.astra.backend.order.CustomerOrderItemRepository items =
            org.mockito.Mockito.mock(com.astra.backend.order.CustomerOrderItemRepository.class);
    private final com.astra.backend.user.AppUserRepository users =
            org.mockito.Mockito.mock(com.astra.backend.user.AppUserRepository.class);
    private final ProductReviewService service = new ProductReviewService(
            reviews, products, orders, items, users);

    @Test
    void storefrontReviewsUseOnlyPublishedReviews() {
        Product product = new Product();
        product.setName("Test product");
        ProductReview review = new ProductReview();
        review.setProduct(product);
        review.setCustomerName("Customer");
        review.setRating(5);
        review.setComment("Excellent");
        review.setPublished(true);
        when(products.findById(4L)).thenReturn(Optional.of(product));
        when(reviews.findByProductIdAndPublishedTrueOrderByCreatedAtDesc(4L))
                .thenReturn(List.of(review));

        assertEquals(1, service.getReviews(4L).size());
        verify(reviews).findByProductIdAndPublishedTrueOrderByCreatedAtDesc(4L);
    }

    @Test
    void adminCanPublishAReview() {
        Product product = new Product();
        product.setName("Test product");
        ProductReview review = new ProductReview();
        review.setProduct(product);
        review.setCustomerName("Customer");
        review.setUserEmail("customer@example.test");
        review.setRating(4);
        review.setComment("Good");
        when(reviews.findById(7L)).thenReturn(Optional.of(review));
        when(reviews.save(review)).thenReturn(review);

        AdminProductReviewResponse response = service.setPublished(7L, true);

        assertEquals(true, response.published());
        assertEquals("Test product", response.productName());
    }

    @Test
    void adminReviewQueueIsPaged() {
        when(reviews.findAllByOrderByCreatedAtDesc(PageRequest.of(0, 20)))
                .thenReturn(new PageImpl<>(List.of()));
        assertEquals(0, service.getAdminReviews(PageRequest.of(0, 20)).getTotalElements());
    }
}
