
package com.astra.backend.monitoring.dto;

public record ApiMonitorStatsResponse(
        long total,
        long successful,
        long failed,
        double averageDuration,
        long frontend,
        long backend
) {
}
