package com.astra.search.repository;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import com.astra.search.document.ProductSearchDocument;

public interface ProductSearchRepository
        extends ElasticsearchRepository<ProductSearchDocument, Long> {
}