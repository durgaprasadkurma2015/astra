package com.astra.recommendation.controller;

import com.astra.dto.RecommendationProductResponse;
import com.astra.recommendation.service.RecommendationService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/recommendations")
public class RecommendationController {

    private final RecommendationService recommendationService;

    public RecommendationController(
            RecommendationService recommendationService
    ) {
        this.recommendationService =
                recommendationService;
    }

    @GetMapping("/trending")
    public ResponseEntity<List<RecommendationProductResponse>>
    trending(
            @RequestParam(defaultValue = "10") int limit
    ) {

        return ResponseEntity.ok(
                recommendationService.getTrending(limit)
        );
    }

    @GetMapping("/for-you")
    public ResponseEntity<List<RecommendationProductResponse>>
    forYou(
            Authentication authentication,
            @RequestParam(defaultValue = "10") int limit
    ) {

        return ResponseEntity.ok(
                recommendationService.getForYou(
                        authentication.getName(),
                        limit
                )
        );
    }

    @GetMapping("/recently-viewed")
    public ResponseEntity<List<RecommendationProductResponse>>
    recentlyViewed(
            Authentication authentication,
            @RequestParam(defaultValue = "10") int limit
    ) {

        return ResponseEntity.ok(
                recommendationService.getRecentlyViewed(
                        authentication.getName(),
                        limit
                )
        );
    }

    @PostMapping("/recently-viewed/{slug}")
    public ResponseEntity<Void> recordView(
            @PathVariable String slug,
            Authentication authentication
    ) {

        String username =
                authentication != null
                        ? authentication.getName()
                        : null;

        recommendationService.recordProductView(
                slug,
                username
        );

        return ResponseEntity.ok().build();
    }

    @GetMapping("/similar/{productId}")
    public ResponseEntity<List<RecommendationProductResponse>>
    similar(
            @PathVariable Long productId,
            @RequestParam(defaultValue = "10") int limit
    ) {

        return ResponseEntity.ok(
                recommendationService.getSimilar(
                        productId,
                        limit
                )
        );
    }
}