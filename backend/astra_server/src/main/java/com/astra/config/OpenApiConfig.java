package com.astra.config;
import io.swagger.v3.oas.models.OpenAPI; import io.swagger.v3.oas.models.info.Info; import io.swagger.v3.oas.models.Components; import io.swagger.v3.oas.models.security.SecurityScheme; import org.springframework.context.annotation.*;
@Configuration public class OpenApiConfig { @Bean OpenAPI openAPI(){return new OpenAPI().info(new Info().title("ASTRA API").version("v1").description("ASTRA e-commerce REST API")).components(new Components().addSecuritySchemes("bearerAuth",new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("JWT")));} }
