package com.astra.recommendation.dto;

import java.util.List;

import com.astra.dto.RecommendationProductResponse;

public record RecommendationResponse(
        String type,
        String title,
        List<RecommendationProductResponse> products
) {
}