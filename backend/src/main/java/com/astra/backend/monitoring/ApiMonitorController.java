
package com.astra.backend.monitoring;

import org.springframework.validation.annotation.Validated;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import com.astra.backend.monitoring.dto.ApiMonitorStatsResponse;
import com.astra.backend.monitoring.dto.ApiRequestLogResponse;

import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;


@RestController
@RequestMapping("/api/v1/admin/monitoring")
@Validated
@PreAuthorize("hasRole('ADMIN')")
public class ApiMonitorController {

    private final ApiRequestMonitorService monitorService;

    public ApiMonitorController(
            ApiRequestMonitorService monitorService
    ) {
        this.monitorService = monitorService;
    }

    /**
     * Search and paginate API request logs.
     *
     * Example:
     * GET /api/v1/admin/monitoring/logs?page=0&size=20
     */
    @GetMapping("/logs")
    public ResponseEntity<Page<ApiRequestLogResponse>> getLogs(

            @RequestParam(defaultValue = "0")
            @Min(0) int page,

            @RequestParam(defaultValue = "20")
            @Min(1) @Max(100) int size,

            @RequestParam(required = false)
            String source,

            @RequestParam(required = false)
            Integer status,

            @RequestParam(required = false)
            String search,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime fromDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
            LocalDateTime toDate
    ) {

        if (fromDate != null && toDate != null
                && fromDate.isAfter(toDate)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "fromDate must be before or equal to toDate"
            );
        }

        PageRequest pageable = PageRequest.of(
                page,
                size,
                Sort.by(Sort.Direction.DESC, "requestTimestamp")
        );

        Page<ApiRequestLogResponse> result =
                monitorService.searchLogs(
                        source,
                        status,
                        search,
                        fromDate,
                        toDate,
                        pageable
                );

        return ResponseEntity.ok(result);
    }

    /**
     * Retrieve one request with its sanitized headers and bodies.
     *
     * GET /api/v1/admin/monitoring/logs/10
     */
    @GetMapping("/logs/{id}")
    public ResponseEntity<ApiRequestLogResponse> getLogDetails(
            @PathVariable Long id
    ) {
        try {
            return ResponseEntity.ok(
                    monitorService.getLog(id)
            );
        } catch (IllegalArgumentException exception) {
            throw new ResponseStatusException(
                    HttpStatus.NOT_FOUND,
                    "API request log not found"
            );
        }
    }

    /**
     * Dashboard statistics for the last 24 hours.
     *
     * GET /api/v1/admin/monitoring/stats
     */
    @GetMapping("/stats")
    public ResponseEntity<ApiMonitorStatsResponse> getStats() {
        return ResponseEntity.ok(
                monitorService.getStats()
        );
    }
}
