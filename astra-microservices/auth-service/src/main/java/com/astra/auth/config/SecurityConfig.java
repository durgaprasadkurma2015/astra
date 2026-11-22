package com.astra.auth.config;

import com.astra.auth.security.CustomUserDetailsService;
import com.astra.auth.security.JwtAuthenticationFilter;
import com.astra.auth.security.JwtService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Bean
    public AuthenticationProvider authenticationProvider(
            CustomUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider =
            new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder);

        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationProvider authenticationProvider) {

        return new ProviderManager(authenticationProvider);
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtService jwtService,
            CustomUserDetailsService userDetailsService)
            throws Exception {

        JwtAuthenticationFilter jwtFilter =
            new JwtAuthenticationFilter(
                jwtService,
                userDetailsService
            );

        http
            .csrf(csrf -> csrf.disable())

            .sessionManagement(session ->
                session.sessionCreationPolicy(
                    SessionCreationPolicy.STATELESS
                )
            )

            .authorizeHttpRequests(auth -> auth

                // Public endpoints
                .requestMatchers(
                    HttpMethod.POST,       
                    "/api/v1/auth/register",
                    "/api/v1/auth/verify-registration-otp",
                    "/api/v1/auth/resend-registration-otp",
                    "/api/v1/auth/forgot-password",
                    "/api/v1/auth/reset-password",
                    "/api/v1/auth/login"
                ).permitAll()
             // Internal Gateway-to-Auth communication.
             // Controller independently validates the internal API key.
             .requestMatchers(
                 HttpMethod.GET,
                 "/internal/auth/tokens/*/revocation-status"
             ).permitAll()
                .requestMatchers(
                    HttpMethod.GET,
                    "/actuator/health",
                    "/api/v1/auth/test"
                ).permitAll()

             // Public Swagger / OpenAPI endpoints
                .requestMatchers(
                    "/v3/api-docs/**",
                    "/v3/api-docs.yaml",
                    "/swagger-ui.html",
                    "/swagger-ui/**",
                    "/webjars/**"
                ).permitAll()



                // Admin-only APIs
                .requestMatchers("/api/v1/admin/**")
                    .hasRole("ADMIN")

                // All other endpoints require a valid login
                .anyRequest().authenticated()
            )

            .exceptionHandling(exception -> exception

                .authenticationEntryPoint((request, response, ex) -> {
                    response.setStatus(401);
                    response.setContentType("application/json");
                    response.getWriter().write(
                        "{\"status\":401,\"error\":\"Unauthorized\",\"message\":\"Authentication required or token invalid\"}"
                    );
                })

                .accessDeniedHandler((request, response, ex) -> {
                    response.setStatus(403);
                    response.setContentType("application/json");
                    response.getWriter().write(
                        "{\"status\":403,\"error\":\"Forbidden\",\"message\":\"You do not have permission to access this resource\"}"
                    );
                })
            )

            .addFilterBefore(
                jwtFilter,
                UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}