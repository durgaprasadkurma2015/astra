
package com.astra.backend.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "ASTRA E-Commerce REST API",
        version = "1.0",
        description = """
            REST API documentation for the ASTRA E-Commerce application.

            Main modules:
            - Authentication and user management
            - Products and categories
            - Shopping cart and wishlist
            - Orders and checkout
            - Coupons and promotions
            - Payments
            - Inventory management
            - File storage
            - Administration

            API base path: /api/v1
            Authentication: JWT Bearer Token
            """
    ),
    tags = {
        @Tag(name = "Authentication", description = "Registration, login, OTP, password reset and token management"),
        @Tag(name = "Products", description = "Product catalogue, search, categories and product details"),
        @Tag(name = "Admin - Products", description = "Administrative product management"),
        @Tag(name = "Cart", description = "Shopping cart operations"),
        @Tag(name = "Wishlist", description = "Customer wishlist operations"),
        @Tag(name = "Orders", description = "Order history and checkout"),
        @Tag(name = "Coupons", description = "Coupon validation and redemption"),
        @Tag(name = "Payments", description = "Payment processing and payment callbacks"),
        @Tag(name = "Reviews", description = "Product reviews and moderation"),
        @Tag(name = "Addresses", description = "Customer address management"),
        @Tag(name = "Inventory", description = "Inventory and stock movement management"),
        @Tag(name = "Files", description = "File upload, download and storage"),
        @Tag(name = "Health", description = "Application health endpoints"),
        @Tag(name = "Admin", description = "Administrative operations")
    },
    security = {
        @SecurityRequirement(name = "bearerAuth")
    }
)
@SecurityScheme(
    name = "bearerAuth",
    type = SecuritySchemeType.HTTP,
    scheme = "bearer",
    bearerFormat = "JWT",
    in = SecuritySchemeIn.HEADER,
    description = "Enter your JWT access token. Do not include the Bearer prefix."
)
public class OpenApiConfig {
}
