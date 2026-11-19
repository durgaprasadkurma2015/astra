
package com.astra.backend.monitoring.dto;

import java.time.LocalDateTime;

import com.astra.backend.monitoring.ApiRequestLog;

public record ApiRequestLogResponse(
        Long id,
        String requestId,
        String source,
        LocalDateTime requestTimestamp,
        String httpMethod,
        String endpoint,
        Integer statusCode,
        Long durationMs,
        Boolean success,
        String clientIp,
        String userAgent,
        String requestHeaders,
        String requestBody,
        String responseHeaders,
        String responseBody,
        String errorMessage,
        LocalDateTime createdAt
) {

    public static ApiRequestLogResponse from(ApiRequestLog log) {
        return new ApiRequestLogResponse(
                log.getId(),
                log.getRequestId(),
                log.getSource(),
                log.getRequestTimestamp(),
                log.getHttpMethod(),
                log.getEndpoint(),
                log.getStatusCode(),
                log.getDurationMs(),
                log.getSuccess(),
                log.getClientIp(),
                log.getUserAgent(),
                log.getRequestHeaders(),
                log.getRequestBody(),
                log.getResponseHeaders(),
                log.getResponseBody(),
                log.getErrorMessage(),
                log.getCreatedAt()
        );
    }
}
