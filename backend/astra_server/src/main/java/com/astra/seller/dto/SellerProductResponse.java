package com.astra.seller.dto;

import java.math.BigDecimal;

public record SellerProductResponse(
        Long id, String name, String slug, String sku,
        BigDecimal price, BigDecimal discountPrice,
        Integer stockQuantity, boolean active, boolean featured,
        Long categoryId, String categoryName
) {}
