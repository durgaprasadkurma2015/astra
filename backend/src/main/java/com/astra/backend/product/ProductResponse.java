package com.astra.backend.product;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public record ProductResponse(
        Long id,
        String name,
        String sku,
        String description,
        String category,
        String subcategory,
        String imageUrl,
        BigDecimal price,
        BigDecimal discountPercentage,
        BigDecimal discountedPrice,
        Integer stockQuantity,
        Boolean active,
        List<ProductVariantResponse> variants,
        LocalDateTime createdAt) {

    public static ProductResponse from(Product product) {
        List<ProductVariantResponse> variants = product.getVariants() == null
                ? List.of()
                : product.getVariants().stream()
                        .map(ProductVariantResponse::from)
                        .toList();

        return new ProductResponse(
                product.getId(),
                product.getName(),
                product.getSku(),
                product.getDescription(),
                product.getCategory(),
                product.getSubcategory(),
                product.getImageUrl(),
                product.getPrice(),
                product.getDiscountPercentage(),
                product.getDiscountedPrice(),
                product.getStockQuantity(),
                product.getActive(),
                variants,
                product.getCreatedAt());
    }

    public record ProductVariantResponse(
            String sku,
            String size,
            String color,
            Integer stockQuantity) {

        private static ProductVariantResponse from(ProductVariant variant) {
            return new ProductVariantResponse(
                    variant.getSku(),
                    variant.getSize(),
                    variant.getColor(),
                    variant.getStockQuantity());
        }
    }
}
