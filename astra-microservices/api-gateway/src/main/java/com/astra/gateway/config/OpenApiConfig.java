package com.astra.gateway.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Astra API Gateway",
        version = "1.0.0",
        description = "Astra Microservices API Documentation"
    )
)
public class OpenApiConfig {
}