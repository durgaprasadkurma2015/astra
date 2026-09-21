package com.astra.search.service;

import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.stereotype.Service;

import com.astra.search.document.ProductSearchDocument;
import com.astra.search.dto.ProductSearchResponse;
import com.astra.search.dto.SearchResponse;

@Service
public class ProductSearchService {

    private final ElasticsearchOperations elasticsearchOperations;

    public ProductSearchService(
            ElasticsearchOperations elasticsearchOperations
    ) {
        this.elasticsearchOperations = elasticsearchOperations;
    }

    public SearchResponse search(
            String keyword,
            int page,
            int size
    ) {

        if (page < 0) {
            page = 0;
        }

        if (size <= 0) {
            size = 20;
        }

        if (size > 100) {
            size = 100;
        }

        Pageable pageable =
                PageRequest.of(page, size);

        String searchText =
                keyword == null
                        ? ""
                        : keyword.trim();

        Query query;

        if (searchText.isBlank()) {

            query = NativeQuery.builder()
                    .withQuery(q -> q.matchAll(m -> m))
                    .withPageable(pageable)
                    .build();

        } else {

            query = NativeQuery.builder()
                    .withQuery(q -> q.multiMatch(m -> m
                            .query(searchText)
                            .fields(
                                    "name",
                                    "description"
                            )
                            .fuzziness("AUTO")
                    ))
                    .withPageable(pageable)
                    .build();
        }

        SearchHits<ProductSearchDocument> hits =
                elasticsearchOperations.search(
                        query,
                        ProductSearchDocument.class
                );

        List<ProductSearchResponse> products =
                new ArrayList<>();

        for (SearchHit<ProductSearchDocument> hit : hits) {

            ProductSearchDocument document =
                    hit.getContent();

            products.add(
                    new ProductSearchResponse(
                            document.getId(),
                            document.getName(),
                            document.getSlug(),
                            document.getDescription(),
                            document.getPrice(),
                            document.getActive()
                    )
            );
        }

        long totalElements =
                hits.getTotalHits();

        int totalPages =
                size == 0
                        ? 0
                        : (int) Math.ceil(
                                (double) totalElements / size
                        );

        return new SearchResponse(
                products,
                page,
                size,
                totalElements,
                totalPages
        );
    }
}