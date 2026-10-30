
package com.astra.backend.order;

import jakarta.validation.constraints.NotBlank;

public record UpdateOrderStatusRequest(
        @NotBlank(message = "Order status is required")
        String orderStatus
) {}
