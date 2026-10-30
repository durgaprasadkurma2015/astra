package com.astra.backend.order;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer_orders")
public class CustomerOrder {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "order_number", nullable = false, unique = true, length = 40)
    private String orderNumber;

    @Column(name = "checkout_idempotency_key", length = 100)
    private String checkoutIdempotencyKey;

    @Column(name = "checkout_request_hash", length = 64)
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
    private String orderStatus = "PLACED";

    @Column(name = "payment_method", nullable = false, length = 30)
    private String paymentMethod = "COD";

    @Column(name = "payment_status", nullable = false, length = 30)
    private String paymentStatus = "PENDING";

    @Column(name = "razorpay_order_id", length = 80, unique = true)
    private String razorpayOrderId;

    @Column(name = "razorpay_payment_id", length = 80)
    private String razorpayPaymentId;

    @Column(name = "created_at", insertable = false, updatable = false)
    private LocalDateTime createdAt;

    public CustomerOrder() {
    }

    public Long getId() {
        return id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public String getCheckoutIdempotencyKey() {
        return checkoutIdempotencyKey;
    }

    public String getCheckoutRequestHash() {
        return checkoutRequestHash;
    }

    public String getUserEmail() {
        return userEmail;
    }

    public String getCustomerName() {
        return customerName;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public String getShippingAddress() {
        return shippingAddress;
    }

    public BigDecimal getSubtotal() {
        return subtotal;
    }

    public String getCouponCode() {
        return couponCode;
    }

    public BigDecimal getDiscountAmount() {
        return discountAmount;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public String getOrderStatus() {
        return orderStatus;
    }

    public String getPaymentMethod() {
        return paymentMethod;
    }

    public String getPaymentStatus() {
        return paymentStatus;
    }

    public String getRazorpayOrderId() { return razorpayOrderId; }

    public String getRazorpayPaymentId() { return razorpayPaymentId; }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setOrderNumber(String value) {
        this.orderNumber = value;
    }

    public void setCheckoutIdempotencyKey(String value) {
        this.checkoutIdempotencyKey = value;
    }

    public void setCheckoutRequestHash(String value) {
        this.checkoutRequestHash = value;
    }

    public void setUserEmail(String value) {
        this.userEmail = value;
    }

    public void setCustomerName(String value) {
        this.customerName = value;
    }

    public void setPhoneNumber(String value) {
        this.phoneNumber = value;
    }

    public void setShippingAddress(String value) {
        this.shippingAddress = value;
    }

    public void setSubtotal(BigDecimal value) {
        this.subtotal = value;
    }

    public void setCouponCode(String value) {
        this.couponCode = value;
    }

    public void setDiscountAmount(BigDecimal value) {
        this.discountAmount = value;
    }

    public void setTotalAmount(BigDecimal value) {
        this.totalAmount = value;
    }

    public void setOrderStatus(String value) {
        this.orderStatus = value;
    }

    public void setPaymentMethod(String value) {
        this.paymentMethod = value;
    }

    public void setPaymentStatus(String value) {
        this.paymentStatus = value;
    }

    public void setRazorpayOrderId(String value) { this.razorpayOrderId = value; }

    public void setRazorpayPaymentId(String value) { this.razorpayPaymentId = value; }
}
