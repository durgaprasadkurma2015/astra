package com.astra.search.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class SearchResponse {

    private List<ProductSearchResponse> products;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
