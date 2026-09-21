package com.astra.dto;

import java.math.BigDecimal;

public record ReviewSummaryResponse(

        Long productId,

        BigDecimal averageRating,

        long reviewCount,

        long fiveStarCount,

        long fourStarCount,

        long threeStarCount,

        long twoStarCount,

        long oneStarCount

) {
}