package com.astra.search.controller;


import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.astra.search.service.ProductIndexService;

@RestController
@RequestMapping("/api/v1/admin/search")
public class AdminSearchController {

    private final ProductIndexService productIndexService;

    public AdminSearchController(
            ProductIndexService productIndexService
    ) {
        this.productIndexService =
                productIndexService;
    }

    @PostMapping("/reindex/products")
    public ResponseEntity<?> reindexProducts() {

        ProductIndexService.IndexResult result =
                productIndexService.reindexAllProducts();

        return ResponseEntity.ok(
                Map.of(
                        "message",
                        "Product re-index completed",

                        "total",
                        result.total(),

                        "indexed",
                        result.indexed(),

                        "failed",
                        result.failed()
                )
        );
    }
}