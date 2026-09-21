package com.astra.search.mapper;

import com.astra.entity.Product;
import com.astra.search.document.ProductSearchDocument;

import org.springframework.stereotype.Component;

@Component
public class ProductSearchMapper {

    public ProductSearchDocument toDocument(Product product) {

        if (product == null) {
            throw new IllegalArgumentException("Product cannot be null");
        }

        return new ProductSearchDocument(
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getDescription(),
                product.getPrice(),
                product.isActive()
        );
    }
}
