package com.astra.product.entity;

import jakarta.persistence.*;

@Embeddable
public class ProductVariant {

    @Column(name = "variant_sku", length = 100)
    private String sku;

    @Column(name = "variant_size", length = 50)
    private String size;

    @Column(name = "variant_color", length = 50)
    private String color;

    @Column(name = "variant_stock_quantity", nullable = false)
    private Integer stockQuantity = 0;

    public ProductVariant() {
    }

    public ProductVariant(
            String sku,
            String size,
            String color,
            Integer stockQuantity) {
        this.sku = sku;
        this.size = size;
        this.color = color;
        this.stockQuantity = stockQuantity;
    }

    public String getSku() { return sku; }
    public String getSize() { return size; }
    public String getColor() { return color; }
    public Integer getStockQuantity() { return stockQuantity; }
}