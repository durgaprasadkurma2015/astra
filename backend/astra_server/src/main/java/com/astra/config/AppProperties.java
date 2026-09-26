package com.astra.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "astra.app")
public record AppProperties(
        String name,
        String environment,
        String frontendUrl
) {
}
