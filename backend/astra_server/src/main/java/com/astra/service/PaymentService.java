package com.astra.service;

import com.astra.dto.PaymentInitiateRequest;
import com.astra.dto.PaymentResponse;
import com.astra.entity.Order;
import com.astra.entity.PaymentTransaction;
import com.astra.entity.Product;
import com.astra.entity.User;
import com.astra.enums.OrderStatus;
import com.astra.enums.PaymentMethod;
import com.astra.enums.PaymentStatus;
import com.astra.integration.PaymentGateway;
import com.astra.integration.PaymentGatewayResult;
import com.astra.repository.OrderRepository;
import com.astra.repository.PaymentTransactionRepository;
import com.astra.repository.ProductRepository;
import com.astra.repository.UserRepository;

import jakarta.transaction.Transactional;

import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@Transactional
public class PaymentService {

    private final PaymentTransactionRepository paymentRepository;
    private final OrderRepository orderRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final PaymentGateway paymentGateway;

    public PaymentService(
            PaymentTransactionRepository paymentRepository,
            OrderRepository orderRepository,
            UserRepository userRepository,
            ProductRepository productRepository,
            PaymentGateway paymentGateway
    ) {
        this.paymentRepository = paymentRepository;
        this.orderRepository = orderRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.paymentGateway = paymentGateway;
    }

    /**
     * Initiate payment for an order.
     */
    public PaymentResponse initiatePayment(
            String email,
            Long orderId,
            PaymentInitiateRequest request
    ) {

        User user = getUser(email);

        Order order = orderRepository
                .findByIdAndUserId(
                        orderId,
                        user.getId()
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Order not found: " + orderId
                        )
                );

        if (order.getStatus() == OrderStatus.CANCELLED) {
            throw new IllegalArgumentException(
                    "Cannot pay for cancelled order"
            );
        }

        if (order.getPaymentStatus() == PaymentStatus.PAID) {
            throw new IllegalArgumentException(
                    "Order has already been paid"
            );
        }

        if (request.paymentMethod() == PaymentMethod.COD) {
            throw new IllegalArgumentException(
                    "COD payment is not implemented in Phase 10"
            );
        }

        /*
         * Prevent duplicate active payments.
         */
        PaymentTransaction existing =
                paymentRepository
                        .findByOrderIdAndUserId(
                                orderId,
                                user.getId()
                        )
                        .orElse(null);

        if (existing != null &&
                existing.getStatus() == PaymentStatus.PENDING) {

            return toResponse(existing);
        }

        PaymentTransaction transaction =
                PaymentTransaction.builder()
                        .transactionNumber(
                                generateTransactionNumber()
                        )
                        .order(order)
                        .user(user)
                        .status(PaymentStatus.PENDING)
                        .paymentMethod(
                                request.paymentMethod()
                        )
                        .amount(order.getTotal())
                        .currency("INR")
                        .build();

        PaymentGatewayResult result =
                paymentGateway.createPayment(transaction);

        if (!result.success()) {

            transaction.setStatus(
                    PaymentStatus.FAILED
            );

            transaction.setFailureReason(
                    result.message()
            );

            PaymentTransaction saved =
                    paymentRepository.save(transaction);

            return toResponse(saved);
        }

        transaction.setProviderOrderId(
                result.providerOrderId()
        );

        PaymentTransaction saved =
                paymentRepository.save(transaction);

        return toResponse(saved);
    }

    /**
     * Get payment.
     */
    @Transactional
    public PaymentResponse getPayment(
            String email,
            Long paymentId
    ) {

        User user = getUser(email);

        PaymentTransaction payment =
                paymentRepository
                        .findByIdAndUserId(
                                paymentId,
                                user.getId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Payment not found: "
                                                + paymentId
                                )
                        );

        return toResponse(payment);
    }

    /**
     * Get payment by order.
     */
    @Transactional
    public PaymentResponse getOrderPayment(
            String email,
            Long orderId
    ) {

        User user = getUser(email);

        PaymentTransaction payment =
                paymentRepository
                        .findByOrderIdAndUserId(
                                orderId,
                                user.getId()
                        )
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Payment not found for order: "
                                                + orderId
                                )
                        );

        return toResponse(payment);
    }

    /**
     * Mock/sandbox payment success.
     */
    public PaymentResponse markSuccess(
            String email,
            Long paymentId
    ) {

        User user = getUser(email);

        PaymentTransaction payment =
                getPaymentEntity(
                        paymentId,
                        user.getId()
                );

        if (payment.getStatus() == PaymentStatus.PAID) {
            return toResponse(payment);
        }

        if (payment.getStatus() == PaymentStatus.REFUNDED) {
            throw new IllegalArgumentException(
                    "Refunded payment cannot be marked paid"
            );
        }

        PaymentGatewayResult result =
                paymentGateway.verifyPayment(payment);

        if (!result.success()) {

            payment.setStatus(
                    PaymentStatus.FAILED
            );

            payment.setFailureReason(
                    result.message()
            );

            return toResponse(
                    paymentRepository.save(payment)
            );
        }

        payment.setStatus(
                PaymentStatus.PAID
        );

        payment.setProviderPaymentId(
                result.providerPaymentId()
        );

        payment.setFailureReason(null);

        Order order = payment.getOrder();

        order.setPaymentStatus(
                PaymentStatus.PAID
        );

        order.setStatus(
                OrderStatus.CONFIRMED
        );

        orderRepository.save(order);

        PaymentTransaction saved =
                paymentRepository.save(payment);

        return toResponse(saved);
    }

    /**
     * Mark payment failed.
     */
    public PaymentResponse markFailed(
            String email,
            Long paymentId,
            String reason
    ) {

        User user = getUser(email);

        PaymentTransaction payment =
                getPaymentEntity(
                        paymentId,
                        user.getId()
                );

        if (payment.getStatus() == PaymentStatus.PAID) {
            throw new IllegalArgumentException(
                    "Paid payment cannot be marked failed"
            );
        }

        payment.setStatus(
                PaymentStatus.FAILED
        );

        payment.setFailureReason(
                reason == null || reason.isBlank()
                        ? "Payment failed"
                        : reason
        );

        return toResponse(
                paymentRepository.save(payment)
        );
    }

    /**
     * Refund paid payment.
     */
    public PaymentResponse refundPayment(
            String email,
            Long paymentId
    ) {

        User user = getUser(email);

        PaymentTransaction payment =
                getPaymentEntity(
                        paymentId,
                        user.getId()
                );

        if (payment.getStatus() != PaymentStatus.PAID) {
            throw new IllegalArgumentException(
                    "Only paid payments can be refunded"
            );
        }

        PaymentGatewayResult result =
                paymentGateway.refundPayment(payment);

        if (!result.success()) {
            throw new IllegalArgumentException(
                    "Refund failed: "
                            + result.message()
            );
        }

        payment.setStatus(
                PaymentStatus.REFUNDED
        );

        payment.setRefundReference(
                result.reference()
        );

        Order order = payment.getOrder();

        order.setPaymentStatus(
                PaymentStatus.REFUNDED
        );

        order.setStatus(
                OrderStatus.CANCELLED
        );

        /*
         * Restore inventory after refund.
         */
        restoreInventory(order);

        orderRepository.save(order);

        return toResponse(
                paymentRepository.save(payment)
        );
    }

    /**
     * Restore product inventory and sales count.
     *
     * Product.salesCount is Long,
     * while stockQuantity is Integer.
     */
    private void restoreInventory(
            Order order
    ) {

        for (var item : order.getItems()) {

            Product product =
                    item.getProduct();

            /*
             * stockQuantity is Integer.
             */
            int currentStock =
                    product.getStockQuantity() == null
                            ? 0
                            : product.getStockQuantity();

            int restoredStock =
                    currentStock + item.getQuantity();

            product.setStockQuantity(
                    restoredStock
            );

            /*
             * salesCount is Long.
             */
            long currentSales =
                    product.getSalesCount() == null
                            ? 0L
                            : product.getSalesCount();

            long restoredSales =
                    Math.max(
                            0L,
                            currentSales - item.getQuantity()
                    );

            product.setSalesCount(
                    restoredSales
            );

            productRepository.save(product);
        }
    }

    private PaymentTransaction getPaymentEntity(
            Long paymentId,
            Long userId
    ) {

        return paymentRepository
                .findByIdAndUserId(
                        paymentId,
                        userId
                )
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Payment not found: "
                                        + paymentId
                        )
                );
    }

    /**
     * Resolve authenticated user.
     *
     * UserRepository defines:
     * findByEmailIgnoreCase(String)
     */
    private User getUser(
            String email
    ) {

        if (email == null || email.isBlank()) {
            throw new IllegalArgumentException(
                    "Authenticated user email is missing"
            );
        }

        return userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Authenticated user not found"
                        )
                );
    }

    private String generateTransactionNumber() {

        String number;

        do {

            number =
                    "TXN-"
                            + UUID.randomUUID()
                            .toString()
                            .replace("-", "")
                            .substring(0, 16)
                            .toUpperCase();

        } while (
                paymentRepository
                        .existsByTransactionNumber(number)
        );

        return number;
    }

    private PaymentResponse toResponse(
            PaymentTransaction payment
    ) {

        return new PaymentResponse(
                payment.getId(),
                payment.getTransactionNumber(),
                payment.getOrder().getId(),
                payment.getOrder().getOrderNumber(),
                payment.getUser().getId(),
                payment.getStatus(),
                payment.getPaymentMethod(),
                payment.getAmount(),
                payment.getCurrency(),
                payment.getProviderPaymentId(),
                payment.getProviderOrderId(),
                payment.getFailureReason(),
                payment.getRefundReference(),
                payment.getCreatedAt(),
                payment.getUpdatedAt()
        );
    }
}
