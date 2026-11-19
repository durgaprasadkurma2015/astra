
package com.astra.backend.monitoring.dto;

import java.time.Instant;

import tools.jackson.databind.JsonNode;

public record ApiRequestLogRequest(
        String requestId,
        Instant timestamp,
        String method,
        String endpoint,
        Integer status,
        Long durationMs,
        Boolean success,
        JsonNode requestHeaders,
        JsonNode requestBody,
        JsonNode responseHeaders,
        JsonNode responseBody,
        String error
) {
}
