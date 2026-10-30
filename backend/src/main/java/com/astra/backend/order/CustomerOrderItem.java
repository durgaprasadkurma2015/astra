
package com.astra.backend.order;

import com.astra.backend.product.Product;
import jakarta.persistence.*;
import java.math.BigDecimal;

@Entity
@Table(name = "customer_order_items")
public class CustomerOrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "order_id", nullable = false)
    private CustomerOrder order;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "line_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal lineTotal;

    public CustomerOrderItem() {}

    public Long getId() { return id; }
    public CustomerOrder getOrder() { return order; }
    public Product getProduct() { return product; }
    public String getProductName() { return productName; }
    public BigDecimal getUnitPrice() { return unitPrice; }
    public Integer getQuantity() { return quantity; }
    public BigDecimal getLineTotal() { return lineTotal; }

    public void setOrder(CustomerOrder value) { order = value; }
    public void setProduct(Product value) { product = value; }
    public void setProductName(String value) { productName = value; }
    public void setUnitPrice(BigDecimal value) { unitPrice = value; }
    public void setQuantity(Integer value) { quantity = value; }
    public void setLineTotal(BigDecimal value) { lineTotal = value; }
}
