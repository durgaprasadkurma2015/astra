package com.astra.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/integrations")
public class IntegrationController {
    @Value("${spring.application.name:astra_server}")
    private String applicationName;

    @GetMapping("/status")
    public Map<String, Object> status() {
        return Map.of(
                "application", applicationName,
                "restClient", "enabled",
                "kafka", "configured",
                "rabbitmq", "configured",
                "redis", "configured",
                "elasticsearch", "configured"
        );
    }
}
