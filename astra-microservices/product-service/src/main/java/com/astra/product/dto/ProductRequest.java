package com.astra.product.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;

public record ProductRequest(

    @NotBlank
    @Size(max = 150)
    String name,

    @Size(max = 100)
    String sku,

    @Size(max = 1000)
    String description,

    @Size(max = 100)
    String category,

    @Size(max = 100)
    String subcategory,

    @Size(max = 1000)
    String imageUrl,

    @NotNull
    @DecimalMin("0.00")
    @Digits(integer = 10, fraction = 2)
    BigDecimal price,

    @DecimalMin("0.00")
    @DecimalMax("100.00")
    @Digits(integer = 3, fraction = 2)
    BigDecimal discountPercentage,

    @NotNull
    @PositiveOrZero
    Integer stockQuantity,

    @Valid
    List<VariantRequest> variants
) {
    public record VariantRequest(
        @Size(max = 100) String sku,
        @Size(max = 50) String size,
        @Size(max = 50) String color,
        @NotNull @PositiveOrZero Integer stockQuantity
    ) {}
}