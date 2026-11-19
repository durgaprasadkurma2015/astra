
package com.astra.order.entity;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "customer_orders",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_customer_orders_number",
            columnNames = "order_number"
        ),
        @UniqueConstraint(
            name = "uk_order_user_idempotency",
            columnNames = {"user_email", "checkout_idempotency_key"}
        )
    }
)
public class CustomerOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", nullable = false, length = 40)
    private String orderNumber;

    @Column(name = "checkout_idempotency_key", nullable = false, length = 100)
    private String checkoutIdempotencyKey;

    @Column(name = "checkout_request_hash", nullable = false, length = 64)
    private String checkoutRequestHash;

    @Column(name = "user_email", nullable = false)
    private String userEmail;

    @Column(name = "customer_name", nullable = false, length = 150)
    private String customerName;

    @Column(name = "phone_number", nullable = false, length = 20)
    private String phoneNumber;

    @Column(name = "shipping_address", nullable = false, columnDefinition = "TEXT")
    private String shippingAddress;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @Column(name = "coupon_code", length = 50)
    private String couponCode;

    @Column(name = "discount_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount;

    @Column(name = "order_status", nullable = false, length = 30)
    private String orderStatus = "PENDING_STOCK";

    @Column(name = "payment_method", nullable = false, length = 30)
    private String paymentMethod = "COD";

    @Column(name = "payment_status", nullable = false, length = 30)
    private String paymentStatus = "PENDING";

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", insertable = false, updatable = false)
    private LocalDateTime updatedAt;

    public Long getId() { return id; }
    public String getOrderNumber() { return orderNumber; }
    public String getCheckoutIdempotencyKey() { return checkoutIdempotencyKey; }
    public String getCheckoutRequestHash() { return checkoutRequestHash; }
    public String getUserEmail() { return userEmail; }
    public String getCustomerName() { return customerName; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getShippingAddress() { return shippingAddress; }
    public BigDecimal getSubtotal() { return subtotal; }
    public String getCouponCode() { return couponCode; }
    public BigDecimal getDiscountAmount() { return discountAmount; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getOrderStatus() { return orderStatus; }
    public String getPaymentMethod() { return paymentMethod; }
    public String getPaymentStatus() { return paymentStatus; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }

    public void setOrderNumber(String value) { orderNumber = value; }
    public void setCheckoutIdempotencyKey(String value) { checkoutIdempotencyKey = value; }
    public void setCheckoutRequestHash(String value) { checkoutRequestHash = value; }
    public void setUserEmail(String value) { userEmail = value; }
    public void setCustomerName(String value) { customerName = value; }
    public void setPhoneNumber(String value) { phoneNumber = value; }
    public void setShippingAddress(String value) { shippingAddress = value; }
    public void setSubtotal(BigDecimal value) { subtotal = value; }
    public void setCouponCode(String value) { couponCode = value; }
    public void setDiscountAmount(BigDecimal value) { discountAmount = value; }
    public void setTotalAmount(BigDecimal value) { totalAmount = value; }
    public void setOrderStatus(String value) { orderStatus = value; }
    public void setPaymentMethod(String value) { paymentMethod = value; }
    public void setPaymentStatus(String value) { paymentStatus = value; }
    
    @PrePersist
    protected void onCreate() {
        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = java.time.LocalDateTime.now();
    }
}

