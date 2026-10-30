package com.astra.backend.order;

import com.astra.backend.order.dto.CheckoutRequest;
import com.astra.backend.order.dto.OrderResponse;
import jakarta.validation.Valid;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.RequestParam;

@RestController
@RequestMapping("/api/v1/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping("/checkout")
    public OrderResponse checkout(
            @RequestHeader("Idempotency-Key") String idempotencyKey,
            @Valid @RequestBody CheckoutRequest request) {

        try {
            return orderService.checkout(request, idempotencyKey);
        } catch (DataIntegrityViolationException exception) {
            // If another concurrent request created the order with the
            // same key, return that order only when the request matches.
            return orderService
                    .findExistingCheckout(idempotencyKey, request)
                    .orElseThrow(() -> exception);
        }
    }

    @GetMapping("/{orderNumber}")
    public OrderResponse getOrder(
            @PathVariable String orderNumber) {
        return orderService.getOrder(orderNumber);
    }

    @PostMapping("/{orderNumber}/payment/verify")
    public OrderResponse verifyPayment(
            @PathVariable String orderNumber,
            @jakarta.validation.Valid @RequestBody RazorpayPaymentVerificationRequest request) {
        return orderService.verifyRazorpayPayment(
                orderNumber, request.razorpayOrderId(),
                request.razorpayPaymentId(), request.razorpaySignature());
    }

    @GetMapping("/my-orders")
    public List<OrderResponse> getMyOrders() {
        return orderService.getMyOrders();
    }

    @GetMapping("/my-orders/page")
    public Page<OrderResponse> getMyOrdersPage(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return orderService.getMyOrders(PageRequest.of(
                Math.max(0, page), Math.min(50, Math.max(1, size)),
                Sort.by(Sort.Direction.DESC, "createdAt")));
    }

    @PutMapping("/{orderNumber}/cancel")
    public OrderResponse cancelOrder(
            @PathVariable String orderNumber) {
        return orderService.cancelOrder(orderNumber);
    }
}
