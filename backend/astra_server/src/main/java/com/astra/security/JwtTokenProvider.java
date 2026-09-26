package com.astra.security;

import com.astra.entity.User;
import org.springframework.stereotype.Component;

@Component
public class JwtTokenProvider {
    private final JwtService jwtService;

    public JwtTokenProvider(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    public String generateToken(User user) {
        return jwtService.generateAccessToken(user);
    }

    public String getSubject(String token) {
        return jwtService.extractSubject(token);
    }

    public boolean validate(String token) {
        return jwtService.isValid(token);
    }
}
