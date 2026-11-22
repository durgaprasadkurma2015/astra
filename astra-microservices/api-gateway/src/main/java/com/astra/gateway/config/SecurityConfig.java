
package com.astra.gateway.config;

import java.util.List;
import java.util.Base64;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.convert.converter.Converter;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusReactiveJwtDecoder;
import org.springframework.security.oauth2.jwt.ReactiveJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;

import com.astra.gateway.security.RevocationCheckingJwtDecoder;

import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Value("${app.jwt.secret}")
    private String jwtSecret;


@Bean
public ReactiveJwtDecoder jwtDecoder(
        @Value("${app.auth.base-url:http://localhost:8081}")
        String authBaseUrl,

        @Value("${app.internal-api-key}")
        String internalApiKey) {

    byte[] keyBytes = Base64.getDecoder().decode(jwtSecret);

    if (keyBytes.length < 32) {
        throw new IllegalStateException(
            "JWT secret must contain at least 32 bytes"
        );
    }

    SecretKey secretKey = new SecretKeySpec(
        keyBytes,
        "HmacSHA256"
    );

    NimbusReactiveJwtDecoder decoder =
        NimbusReactiveJwtDecoder.withSecretKey(secretKey)
            .macAlgorithm(MacAlgorithm.HS256)
            .build();

    decoder.setJwtValidator(JwtValidators.createDefault());

    return new RevocationCheckingJwtDecoder(
        decoder,
        org.springframework.web.reactive.function.client.WebClient
            .builder()
            .baseUrl(authBaseUrl)
            .build(),
        internalApiKey
    );
}

    @Bean
    public Converter<Jwt, Mono<AbstractAuthenticationToken>>
    jwtAuthenticationConverter() {

        return jwt -> {

            String role = jwt.getClaimAsString("role");

            List<SimpleGrantedAuthority> authorities =
                role == null || role.isBlank()
                    ? List.of()
                    : List.of(
                        new SimpleGrantedAuthority("ROLE_" + role)
                    );

            return Mono.just(
                new JwtAuthenticationToken(
                    jwt,
                    authorities,
                    jwt.getSubject()
                )
            );
        };
    }

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http,
            ReactiveJwtDecoder jwtDecoder,
            Converter<Jwt, Mono<AbstractAuthenticationToken>>
                jwtAuthenticationConverter) {

        return http
            .csrf(ServerHttpSecurity.CsrfSpec::disable)

            .cors(cors -> {})

            .authorizeExchange(exchange -> exchange

                .pathMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                // Public authentication endpoints
                .pathMatchers(
                    HttpMethod.POST,
                    "/api/v1/auth/register",
                    "/api/v1/auth/verify-registration-otp",
                    "/api/v1/auth/resend-registration-otp",
                    "/api/v1/auth/login"
                ).permitAll()

                .pathMatchers(
                    HttpMethod.GET,
                    "/actuator/health"
                ).permitAll()

             
                
             // Public Swagger documentation endpoints
                .pathMatchers(
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/v3/api-docs/**",
                    "/v3/api-docs.yaml",
                    "/auth/v3/api-docs",
                    "/customer/v3/api-docs",
                    "/product/v3/api-docs",
                    "/inventory/v3/api-docs",
                    "/cart/v3/api-docs",
                    "/wishlist/v3/api-docs",
                    "/order/v3/api-docs",
                    "/promotion/v3/api-docs",
                    "/notification/v3/api-docs",
                    "/review/v3/api-docs",
                    "/payment/v3/api-docs"
                ).permitAll()
                
                // Admin-only endpoints
                .pathMatchers("/api/v1/admin/**")
                    .hasRole("ADMIN")

                .pathMatchers("/actuator/**")
                    .hasRole("ADMIN")

                // All remaining endpoints require authentication
                .anyExchange().authenticated()
            )

            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(unauthorizedHandler())
                .accessDeniedHandler(forbiddenHandler())
            )

            .oauth2ResourceServer(oauth2 -> oauth2
                .jwt(jwt -> jwt
                    .jwtDecoder(jwtDecoder)
                    .jwtAuthenticationConverter(
                        jwtAuthenticationConverter
                    )
                )
            )

            .build();
    }

    @Bean
    public ServerAuthenticationEntryPoint unauthorizedHandler() {

        return (exchange, ex) -> {

            var response = exchange.getResponse();

            response.setStatusCode(HttpStatus.UNAUTHORIZED);

            response.getHeaders().setContentType(
                MediaType.APPLICATION_JSON
            );

            byte[] body = """
                {"status":401,"error":"Unauthorized","message":"Missing, invalid or expired JWT token"}
                """.trim().getBytes();

            return response.writeWith(
                Mono.just(response.bufferFactory().wrap(body))
            );
        };
    }

    @Bean
    public ServerAccessDeniedHandler forbiddenHandler() {

        return (exchange, ex) -> {

            var response = exchange.getResponse();

            response.setStatusCode(HttpStatus.FORBIDDEN);

            response.getHeaders().setContentType(
                MediaType.APPLICATION_JSON
            );

            byte[] body = """
                {"status":403,"error":"Forbidden","message":"You do not have permission to access this resource"}
                """.trim().getBytes();

            return response.writeWith(
                Mono.just(response.bufferFactory().wrap(body))
            );
        };
    }
}
