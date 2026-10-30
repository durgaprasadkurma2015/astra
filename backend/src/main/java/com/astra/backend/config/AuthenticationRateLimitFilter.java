package com.astra.backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class AuthenticationRateLimitFilter extends OncePerRequestFilter {
    private static final int TOO_MANY_REQUESTS = 429;
    private final ObjectMapper objectMapper;
    private final int windowSeconds;
    private final int loginLimit;
    private final int recoveryLimit;
    private final ConcurrentHashMap<String, Window> windows = new ConcurrentHashMap<>();

    public AuthenticationRateLimitFilter(ObjectMapper objectMapper,
            @Value("${app.security.auth-rate-limit-window-seconds:60}") int windowSeconds,
            @Value("${app.security.login-rate-limit:10}") int loginLimit,
            @Value("${app.security.recovery-rate-limit:5}") int recoveryLimit) {
        this.objectMapper = objectMapper;
        this.windowSeconds = windowSeconds;
        this.loginLimit = loginLimit;
        this.recoveryLimit = recoveryLimit;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
            HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        String path = request.getServletPath();
        if (!"POST".equalsIgnoreCase(request.getMethod()) || !isLimitedPath(path)) {
            chain.doFilter(request, response);
            return;
        }

        long now = System.currentTimeMillis();
        long windowMillis = Math.max(1, windowSeconds) * 1000L;
        String key = request.getRemoteAddr() + ":" + path;
        Window window = windows.compute(key, (ignored, existing) ->
                existing == null || now - existing.startedAt >= windowMillis
                        ? new Window(now) : existing);

        int limit = path.endsWith("/login") ? loginLimit : recoveryLimit;
        if (!window.allow(now, windowMillis, limit)) {
            response.setStatus(TOO_MANY_REQUESTS);
            response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            objectMapper.writeValue(response.getWriter(), Map.of(
                    "title", "Too many requests",
                    "status", TOO_MANY_REQUESTS,
                    "detail", "Please wait before trying again."));
            return;
        }

        if (windows.size() > 5000) {
            windows.entrySet().removeIf(entry ->
                    now - entry.getValue().startedAt > windowMillis * 2);
        }
        chain.doFilter(request, response);
    }

    private boolean isLimitedPath(String path) {
        return path.equals("/api/v1/auth/login")
                || path.equals("/api/v1/auth/register")
                || path.equals("/api/v1/auth/verify-registration-otp")
                || path.equals("/api/v1/auth/resend-registration-otp")
                || path.equals("/api/v1/auth/forgot-password")
                || path.equals("/api/v1/auth/verify-otp")
                || path.equals("/api/v1/auth/reset-password");
    }

    private static final class Window {
        private long startedAt;
        private int requests;

        private Window(long startedAt) { this.startedAt = startedAt; }

        private synchronized boolean allow(long now, long duration, int limit) {
            if (now - startedAt >= duration) {
                startedAt = now;
                requests = 0;
            }
            return ++requests <= Math.max(1, limit);
        }
    }
}
