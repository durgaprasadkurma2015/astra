package com.astra.backend.config;

import java.util.Locale;

import com.astra.backend.user.AppUser;
import com.astra.backend.user.AppUserRepository;
import com.astra.backend.user.Role;

import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class AdminBootstrapRunner {

    @Bean
    public CommandLineRunner bootstrapAdmin(
            AppUserRepository userRepository,
            PasswordEncoder passwordEncoder) {

        return args -> {
            String enabled = System.getenv("ASTRA_BOOTSTRAP_ADMIN_ENABLED");

            // Do nothing unless explicitly enabled.
            if (!"true".equalsIgnoreCase(enabled)) {
                return;
            }

            boolean adminExists = userRepository.findAll()
                    .stream()
                    .anyMatch(user -> user.getRole() == Role.ADMIN);

            if (adminExists) {
                System.out.println(
                        "ASTRA bootstrap skipped: an ADMIN already exists.");
                return;
            }

            String email = System.getenv("ASTRA_BOOTSTRAP_ADMIN_EMAIL");
            String password = System.getenv("ASTRA_BOOTSTRAP_ADMIN_PASSWORD");

            if (email == null || email.isBlank()
                    || password == null || password.length() < 8
                    || password.length() > 72) {
                throw new IllegalStateException(
                        "Set a valid bootstrap admin email and password "
                        + "through environment variables.");
            }

            email = email.trim().toLowerCase(Locale.ROOT);

            if (userRepository.existsByEmail(email)) {
                throw new IllegalStateException(
                        "Bootstrap email already belongs to an existing user. "
                        + "Use a different email.");
            }

            AppUser admin = new AppUser();
            admin.setFullName("ASTRA Administrator");
            admin.setEmail(email);
            admin.setPasswordHash(passwordEncoder.encode(password));
            admin.setRole(Role.ADMIN);
//            admin.setActive(true);
            admin.setEmailVerified(true);

            userRepository.saveAndFlush(admin);

            System.out.println(
                    "ASTRA bootstrap admin created successfully.");
        };
    }
}