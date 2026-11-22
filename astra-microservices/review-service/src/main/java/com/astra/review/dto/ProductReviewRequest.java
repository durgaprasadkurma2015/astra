
package com.astra.review.dto;

import jakarta.validation.constraints.*;

public record ProductReviewRequest(

    @NotNull(message = "Rating is required")
    @Min(value = 1, message = "Rating must be at least 1")
    @Max(value = 5, message = "Rating cannot exceed 5")
    Integer rating,

    @NotBlank(message = "Review comment is required")
    @Size(max = 2000, message = "Review comment cannot exceed 2000 characters")
    String comment

) {}
