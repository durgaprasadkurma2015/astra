
package com.astra.inventory.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.util.List;

public record ReservationRequest(
    @NotBlank String orderNumber,

    @NotEmpty
    List<@Valid ReservationItemRequest> items
) {
    public record ReservationItemRequest(
        @NotNull Long productId,
        @Min(1) int quantity
    ) {}
}
