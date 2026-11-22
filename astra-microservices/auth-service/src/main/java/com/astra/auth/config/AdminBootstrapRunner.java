
package com.astra.auth.config;

import com.astra.auth.entity.AppUser;
import com.astra.auth.entity.Role;
import com.astra.auth.repository.AppUserRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@ConditionalOnProperty(
    name = "astra.bootstrap.admin.enabled",
    havingValue = "true"
)
public class AdminBootstrapRunner implements CommandLineRunner {

    private final AppUserRepository repository;
    private final PasswordEncoder passwordEncoder;

    @Value("${ASTRA_BOOTSTRAP_ADMIN_EMAIL}")
    private String adminEmail;

    @Value("${ASTRA_BOOTSTRAP_ADMIN_PASSWORD}")
    private String adminPassword;

    @Value("${ASTRA_BOOTSTRAP_ADMIN_FULL_NAME:Astra Administrator}")
    private String adminFullName;

    public AdminBootstrapRunner(
            AppUserRepository repository,
            PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {

        String email = adminEmail.trim().toLowerCase();

        if (adminPassword == null || adminPassword.length() < 12) {
            throw new IllegalStateException(
                "Bootstrap Admin password must contain at least 12 characters."
            );
        }

        var existingUser = repository.findByEmail(email);

        if (existingUser.isPresent()) {
            AppUser user = existingUser.get();

            if (user.getRole() != Role.ADMIN) {
                throw new IllegalStateException(
                    "Admin bootstrap refused: email already belongs to a non-admin user."
                );
            }

            System.out.println(
                "Astra Admin already exists. No changes made."
            );
            return;
        }

        AppUser admin = new AppUser();

        admin.setFullName(adminFullName);
        admin.setEmail(email);
        admin.setPasswordHash(passwordEncoder.encode(adminPassword));
        admin.setRole(Role.ADMIN);
        admin.setEmailVerified(true);

        // AppUser.active defaults to true.

        repository.save(admin);

        System.out.println(
            "Astra default Admin account created successfully."
        );
    }
}
