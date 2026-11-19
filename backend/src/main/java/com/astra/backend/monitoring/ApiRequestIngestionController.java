
package com.astra.backend.monitoring;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.astra.backend.monitoring.dto.ApiRequestLogRequest;

@RestController
@RequestMapping("/api/v1/monitoring")
public class ApiRequestIngestionController {

    private final ApiRequestMonitorService monitorService;

    public ApiRequestIngestionController(
            ApiRequestMonitorService monitorService
    ) {
        this.monitorService = monitorService;
    }

    @PostMapping("/frontend")
    public ResponseEntity<Void> ingestFrontendLog(
            @Valid @RequestBody ApiRequestLogRequest request,
            HttpServletRequest servletRequest
    ) {
        monitorService.saveFrontendLog(request, servletRequest);

        return ResponseEntity.accepted().build();
    }
}
