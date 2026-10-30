package com.astra.backend.auth;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import java.time.Duration;
import java.util.Arrays;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
import org.springframework.stereotype.Component;

@Component
public class RefreshCookieService {
    private final String cookieName;
    private final String sameSite;
    private final boolean secure;

    public RefreshCookieService(
            @Value("${app.auth.refresh-cookie-name:ASTRA_REFRESH}") String cookieName,
            @Value("${app.auth.cookie-same-site:Lax}") String sameSite,
            @Value("${app.auth.cookie-secure:false}") boolean secure) {
        this.cookieName = cookieName;
        this.sameSite = sameSite;
        this.secure = secure;
    }

    public String read(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies == null) return null;
        return Arrays.stream(cookies)
                .filter(cookie -> cookieName.equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);
    }

    public ResponseCookie issue(AuthService.AuthSession session) {
        ResponseCookie.ResponseCookieBuilder cookie = ResponseCookie
                .from(cookieName, session.refreshToken())
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path("/api/v1/auth");
        if (session.persistent()) {
            cookie.maxAge(Duration.ofDays(30));
        }
        return cookie.build();
    }

    public ResponseCookie clear() {
        return ResponseCookie.from(cookieName, "")
                .httpOnly(true)
                .secure(secure)
                .sameSite(sameSite)
                .path("/api/v1/auth")
                .maxAge(Duration.ZERO)
                .build();
    }
}
