package com.astra.auth.security;

import com.astra.auth.entity.AppUser;
import com.astra.auth.repository.AppUserRepository;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final AppUserRepository userRepository;

    public CustomUserDetailsService(AppUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {

        AppUser user = userRepository.findByEmail(
            email.trim().toLowerCase(Locale.ROOT)
        ).orElseThrow(() ->
            new UsernameNotFoundException("User not found")
        );

        return new CustomUserDetails(user);
    }
}