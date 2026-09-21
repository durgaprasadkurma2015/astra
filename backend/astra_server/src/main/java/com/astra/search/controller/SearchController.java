package com.astra.search.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.astra.search.dto.AutocompleteResponse;
import com.astra.search.dto.SearchResponse;
import com.astra.search.dto.SearchSuggestionResponse;
import com.astra.search.service.ProductSearchService;
import com.astra.search.service.SearchSuggestionService;

@RestController
@RequestMapping("/api/v1/search")
public class SearchController {

    private final ProductSearchService productSearchService;
    private final SearchSuggestionService suggestionService;

    public SearchController(
            ProductSearchService productSearchService,
            SearchSuggestionService suggestionService
    ) {
        this.productSearchService =
                productSearchService;

        this.suggestionService =
                suggestionService;
    }

    @GetMapping("/products")
    public ResponseEntity<SearchResponse> searchProducts(

            @RequestParam(required = false)
            String q,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "20")
            int size

    ) {

        return ResponseEntity.ok(
                productSearchService.search(
                        q,
                        page,
                        size
                )
        );
    }

    @GetMapping("/autocomplete")
    public ResponseEntity<AutocompleteResponse> autocomplete(

            @RequestParam String q

    ) {

        return ResponseEntity.ok(
                suggestionService.autocomplete(q)
        );
    }

    @GetMapping("/suggestions")
    public ResponseEntity<SearchSuggestionResponse> suggestions(

            @RequestParam String q

    ) {

        return ResponseEntity.ok(
                suggestionService.suggestions(q)
        );
    }
}