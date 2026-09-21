package com.astra.search.service;



import java.util.ArrayList;
import java.util.List;

import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.stereotype.Service;

import com.astra.search.document.ProductSearchDocument;
import com.astra.search.dto.AutocompleteResponse;
import com.astra.search.dto.SearchSuggestionResponse;

@Service
public class SearchSuggestionService {

    private final ElasticsearchOperations elasticsearchOperations;

    public SearchSuggestionService(
            ElasticsearchOperations elasticsearchOperations
    ) {
        this.elasticsearchOperations =
                elasticsearchOperations;
    }

    public AutocompleteResponse autocomplete(
            String keyword
    ) {

        String searchText =
                keyword == null
                        ? ""
                        : keyword.trim();

        if (searchText.isBlank()) {

            return new AutocompleteResponse(
                    List.of()
            );
        }

        Query query =
                NativeQuery.builder()
                        .withQuery(q -> q.matchPhrasePrefix(
                                m -> m
                                        .field("name")
                                        .query(searchText)
                        ))
                        .withMaxResults(10)
                        .build();

        SearchHits<ProductSearchDocument> hits =
                elasticsearchOperations.search(
                        query,
                        ProductSearchDocument.class
                );

        List<String> suggestions =
                new ArrayList<>();

        for (SearchHit<ProductSearchDocument> hit : hits) {

            String name =
                    hit.getContent().getName();

            if (name != null &&
                    !suggestions.contains(name)) {

                suggestions.add(name);
            }
        }

        return new AutocompleteResponse(
                suggestions
        );
    }

    public SearchSuggestionResponse suggestions(
            String keyword
    ) {

        AutocompleteResponse response =
                autocomplete(keyword);

        return new SearchSuggestionResponse(
                response.suggestions()
        );
    }
}
