package com.astra.product.service;

import com.astra.entity.Product;
import com.astra.repository.CategoryRepository;
import com.astra.repository.ProductImageRepository;
import com.astra.repository.ProductInventoryRepository;
import com.astra.repository.ProductRepository;
import com.astra.event.ProductEventPublisher;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

class ProductServiceTest {
    @Test
    void constructorCreatesService() {
        ProductService service = new ProductService(
                Mockito.mock(ProductRepository.class),
                Mockito.mock(CategoryRepository.class),
                Mockito.mock(ProductImageRepository.class),
                Mockito.mock(ProductInventoryRepository.class),
                Mockito.mock(ProductEventPublisher.class)
        );
        assertNotNull(service);
    }
}
