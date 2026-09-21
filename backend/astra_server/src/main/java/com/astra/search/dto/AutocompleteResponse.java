package com.astra.search.dto;



import java.util.List;

public record AutocompleteResponse(
        List<String> suggestions
) {
}