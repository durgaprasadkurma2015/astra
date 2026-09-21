package com.astra.security;

import com.astra.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.*;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserRepository userRepository;

    public JwtAuthenticationFilter(JwtService jwtService, UserRepository userRepository) {
        this.jwtService = jwtService;
        this.userRepository = userRepository;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        String header = request.getHeader("Authorization");

        if (header != null && header.startsWith("Bearer ")) {
            String token = header.substring(7);

            if (jwtService.isValid(token)) {
                String email = jwtService.extractSubject(token);
                userRepository.findByEmailIgnoreCase(email).ifPresent(user -> {
                    if (user.isEnabled()) {
                        var authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().name());
                        var authentication = new UsernamePasswordAuthenticationToken(
                                user.getEmail(), null, List.of(authority));
                        SecurityContextHolder.getContext().setAuthentication(authentication);
                    }
                });
            }
        }

        filterChain.doFilter(request, response);
    }
}
