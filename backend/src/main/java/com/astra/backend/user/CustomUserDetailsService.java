package com.astra.backend.user;

import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService
        implements UserDetailsService {

    private final AppUserRepository repository;

    public CustomUserDetailsService(AppUserRepository repository) {
        this.repository = repository;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        AppUser appUser = repository.findByEmail(email)
            .orElseThrow(() ->
                new UsernameNotFoundException("User not found")
            );

        return User.withUsername(appUser.getEmail())
            .password(appUser.getPasswordHash())
            .roles(appUser.getRole().name())
            .disabled(!appUser.isActive() || !appUser.isEmailVerified())
            .build();
    }
}