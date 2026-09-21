package com.astra.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Size;

public record ReviewRequest(

        @Min(value = 1, message = "Rating must be at least 1")
        @Max(value = 5, message = "Rating must not exceed 5")
        Integer rating,

        @Size(
            max = 200,
            message = "Review title must not exceed 200 characters"
        )
        String title,

        @Size(
            max = 5000,
            message = "Review comment must not exceed 5000 characters"
        )
        String comment

) {
}