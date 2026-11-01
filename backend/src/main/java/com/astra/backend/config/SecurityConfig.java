
package com.astra.backend.config;

import java.util.List;
import org.springframework.beans.factory.annotation.Value;

import com.astra.backend.auth.JwtAuthenticationFilter;
import com.astra.backend.user.CustomUserDetailsService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider(
            CustomUserDetailsService userDetailsService,
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider provider =
                new DaoAuthenticationProvider(userDetailsService);

        provider.setPasswordEncoder(passwordEncoder);
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration configuration)
            throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(
                List.of(frontendUrl.trim()));

        configuration.setAllowedMethods(
                List.of(
                        "GET",
                        "POST",
                        "PUT",
                        "PATCH",
                        "DELETE",
                        "OPTIONS"
                ));

        configuration.setAllowedHeaders(
                List.of(
                        "Authorization",
                        "Content-Type",
                        "Idempotency-Key"
                ));

        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source =
                new UrlBasedCorsConfigurationSource();

        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtFilter,
            DaoAuthenticationProvider authenticationProvider)
            throws Exception {

        http
            .cors(cors -> cors.configurationSource(
                    corsConfigurationSource()))
            .csrf(csrf -> csrf.disable())
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())
            .sessionManagement(session ->
                    session.sessionCreationPolicy(
                            SessionCreationPolicy.STATELESS))
            .authenticationProvider(authenticationProvider)
            .authorizeHttpRequests(auth -> auth

                .requestMatchers(
                        "/api/v1/health",
                        "/actuator/health",
                        "/actuator/health/**",
                        "/actuator/info",
                        "/api/v1/auth/register",
                        "/api/v1/auth/verify-registration-otp",
                        "/api/v1/auth/resend-registration-otp",
                        "/api/v1/auth/login",
                        "/api/v1/auth/refresh",
                        "/api/v1/auth/logout",
                        "/api/v1/auth/forgot-password",
                        "/api/v1/auth/verify-otp",
                        "/api/v1/auth/reset-password",
                        "/api/v1/webhooks/razorpay"
                ).permitAll()
                .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                .requestMatchers(
                        "/api/v1/auth/change-password"
                ).authenticated()

                .requestMatchers(
                        "/v3/api-docs/**",
                        "/v3/api-docs.yaml",
                        "/swagger-ui/**",
                        "/swagger-ui.html"
                ).permitAll()

                .requestMatchers(
                        HttpMethod.OPTIONS, "/**"
                ).permitAll()

                .requestMatchers(
                        HttpMethod.GET,
                        "/api/v1/products",
                        "/api/v1/products/**"
                ).permitAll()
                .requestMatchers(
                        "/api/v1/files/public/**"
                ).permitAll()

                .requestMatchers(
                        "/api/v1/files/admin/**"
                ).hasRole("ADMIN")

                .requestMatchers(
                        "/api/v1/files/me/**"
                ).authenticated()

                .requestMatchers(
                        "/api/v1/files/*/download"
                ).authenticated()
                .requestMatchers(
                	    "/api/v1/files/admin",
                	    "/api/v1/files/admin/**"
                	).hasRole("ADMIN")
                .requestMatchers("/actuator/**").hasRole("ADMIN")
                	.requestMatchers(
                	    "/api/v1/files/me",
                	    "/api/v1/files/me/**"
                	).authenticated()
                .anyRequest().authenticated()
            )
            .addFilterBefore(
                    jwtFilter,
                    UsernamePasswordAuthenticationFilter.class
            );

        return http.build();
    }
}
