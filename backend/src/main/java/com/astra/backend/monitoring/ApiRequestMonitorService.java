
package com.astra.backend.monitoring;

import com.astra.backend.monitoring.dto.ApiMonitorStatsResponse;
import com.astra.backend.monitoring.dto.ApiRequestLogRequest;
import com.astra.backend.monitoring.dto.ApiRequestLogResponse;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.JsonNode;

import jakarta.servlet.http.HttpServletRequest;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.UUID;

@Service
public class ApiRequestMonitorService {

    private final ApiRequestLogRepository repository;
    private final ObjectMapper objectMapper;

    public ApiRequestMonitorService(
            ApiRequestLogRepository repository,
            ObjectMapper objectMapper
    ) {
        this.repository = repository;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public void saveFrontendLog(
            ApiRequestLogRequest request,
            HttpServletRequest servletRequest
    ) {
        ApiRequestLog log = ApiRequestLog.create(
                "FRONTEND",
                request.method().toUpperCase(),
                ApiLogSanitizer.sanitizeEndpoint(request.endpoint()),
                request.status(),
                request.durationMs()
        );

        log.setRequestId(
                request.requestId() != null
                        ? request.requestId()
                        : UUID.randomUUID().toString()
        );

        log.setRequestTimestamp(
                LocalDateTime.ofInstant(
                        request.timestamp(),
                        ZoneOffset.UTC
                )
        );

        log.setSuccess(request.success());

        // Never trust an IP address supplied by the browser.
        log.setClientIp(resolveClientIp(servletRequest));

        log.setUserAgent(
                ApiLogSanitizer.sanitizeText(
                        servletRequest.getHeader("User-Agent")
                )
        );

        log.setRequestHeaders(
                ApiLogSanitizer.sanitizeJson(
                        request.requestHeaders(),
                        objectMapper
                )
        );

        log.setRequestBody(
                ApiLogSanitizer.sanitizeJson(
                        request.requestBody(),
                        objectMapper
                )
        );

        log.setResponseHeaders(
                ApiLogSanitizer.sanitizeJson(
                        request.responseHeaders(),
                        objectMapper
                )
        );

        log.setResponseBody(
                ApiLogSanitizer.sanitizeJson(
                        request.responseBody(),
                        objectMapper
                )
        );

        log.setErrorMessage(
                ApiLogSanitizer.sanitizeText(request.error())
        );

        repository.save(log);
    }

    @Transactional(readOnly = true)
    public Page<ApiRequestLogResponse> searchLogs(
            String source,
            Integer status,
            String search,
            LocalDateTime fromDate,
            LocalDateTime toDate,
            Pageable pageable
    ) {
        String normalizedSource = normalizeSource(source);

        Page<ApiRequestLog> results = repository.searchLogs(
                normalizedSource,
                status,
                normalizeSearch(search),
                fromDate,
                toDate,
                pageable
        );

        return results.map(ApiRequestLogResponse::from);
    }

    @Transactional(readOnly = true)
    public ApiRequestLogResponse getLog(Long id) {
        ApiRequestLog log = repository.findById(id)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "API request log not found: " + id
                        )
                );

        return ApiRequestLogResponse.from(log);
    }

@Transactional
public void saveBackendLog(ApiRequestLog log) {
    if (log == null) {
        return;
    }

    repository.save(log);
}

    @Transactional(readOnly = true)
    public ApiMonitorStatsResponse getStats() {
        LocalDateTime fromDate = LocalDateTime.now(ZoneOffset.UTC)
                .minusHours(24);

        long total = repository.countSince(fromDate);
        long successful = repository.countSuccessfulSince(fromDate);
        long failed = repository.countFailedSince(fromDate);

        double average = repository.averageDurationSince(fromDate) != null
                ? repository.averageDurationSince(fromDate)
                : 0.0;

        long frontend = repository
                .countBySourceAndRequestTimestampGreaterThanEqual(
                        "FRONTEND",
                        fromDate
                );

        long backend = repository
                .countBySourceAndRequestTimestampGreaterThanEqual(
                        "BACKEND",
                        fromDate
                );

        return new ApiMonitorStatsResponse(
                total,
                successful,
                failed,
                average,
                frontend,
                backend
        );
    }

    private String normalizeSource(String source) {
        if (source == null || source.isBlank()
                || "ALL".equalsIgnoreCase(source)) {
            return null;
        }

        String normalized = source.trim().toUpperCase();

        if (!normalized.equals("FRONTEND")
                && !normalized.equals("BACKEND")) {
            throw new IllegalArgumentException(
                    "Invalid source. Use FRONTEND or BACKEND."
            );
        }

        return normalized;
    }

    private String normalizeSearch(String search) {
        if (search == null || search.isBlank()) {
            return null;
        }

        return search.trim();
    }

    private String resolveClientIp(HttpServletRequest request) {
        // Remote address is used rather than blindly trusting
        // X-Forwarded-For supplied by the client.
        String ip = request.getRemoteAddr();

        if (ip == null || ip.isBlank()) {
            return "UNKNOWN";
        }

        return ip.length() > 64 ? ip.substring(0, 64) : ip;
    }
}
