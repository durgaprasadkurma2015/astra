package com.astra.search.dto;

import java.util.List;

public record SearchSuggestionResponse(
        List<String> suggestions
) {
}