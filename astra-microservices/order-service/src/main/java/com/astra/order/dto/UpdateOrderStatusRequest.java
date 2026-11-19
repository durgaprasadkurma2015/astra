
package com.astra.order.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateOrderStatusRequest(
    @NotBlank(message = "Order status is required")
    String orderStatus
) {}
