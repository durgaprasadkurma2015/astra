package com.astra.backend.product;

import jakarta.persistence.*;
import java.util.Objects;

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
        Integer stockQuantity
    ) {
        this.sku = sku;
        this.size = size;
        this.color = color;
        this.stockQuantity = stockQuantity == null ? 0 : stockQuantity;
    }

    public String getSku() {
        return sku;
    }

    public String getSize() {
        return size;
    }

    public String getColor() {
        return color;
    }

    public Integer getStockQuantity() {
        return stockQuantity;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public void setSize(String size) {
        this.size = size;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public void setStockQuantity(Integer stockQuantity) {
        this.stockQuantity = stockQuantity;
    }

    @Override
    public boolean equals(Object object) {
        if (this == object) {
            return true;
        }

        if (!(object instanceof ProductVariant other)) {
            return false;
        }

        return Objects.equals(sku, other.sku)
            && Objects.equals(size, other.size)
            && Objects.equals(color, other.color)
            && Objects.equals(stockQuantity, other.stockQuantity);
    }

    @Override
    public int hashCode() {
        return Objects.hash(sku, size, color, stockQuantity);
    }
}