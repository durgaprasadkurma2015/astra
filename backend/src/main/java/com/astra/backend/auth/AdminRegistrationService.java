package com.astra.backend.auth;

import java.util.Locale;

import com.astra.backend.audit.AuditService;
import com.astra.backend.auth.dto.AdminRegistrationRequest;
import com.astra.backend.auth.dto.AdminRegistrationResponse;
import com.astra.backend.user.AppUser;
import com.astra.backend.user.AppUserRepository;
import com.astra.backend.user.Role;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AdminRegistrationService {

    private final AppUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public AdminRegistrationService(
            AppUserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuditService auditService) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.auditService = auditService;
    }

    @Transactional
    public AdminRegistrationResponse registerAdmin(
            AdminRegistrationRequest request,
            String registeredByEmail) {

        String fullName = request.fullName().trim();

        String email = request.email()
                .trim()
                .toLowerCase(Locale.ROOT);

        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "An account with this email already exists"
            );
        }

        AppUser admin = new AppUser();

        admin.setFullName(fullName);
        admin.setEmail(email);

        // Never store the raw password.
        admin.setPasswordHash(
                passwordEncoder.encode(request.password())
        );

        // Role is assigned only by the backend.
        admin.setRole(Role.ADMIN);

        // This endpoint is available only to an authenticated admin.
        // Admin account creation is performed by an authorized admin.
        admin.setEmailVerified(true);

        AppUser savedAdmin;

        try {
            savedAdmin = userRepository.saveAndFlush(admin);
        } catch (DataIntegrityViolationException ex) {
            // Handles a possible duplicate-email race condition.
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "An account with this email already exists"
            );
        }

        auditService.log(
                registeredByEmail,
                "ADMIN_REGISTRATION",
                "SUCCESS",
                "Created admin account with id " + savedAdmin.getId(),
                null
        );

        return new AdminRegistrationResponse(
                savedAdmin.getId(),
                savedAdmin.getFullName(),
                savedAdmin.getEmail(),
                savedAdmin.getRole().name(),
                savedAdmin.isActive(),
                savedAdmin.isEmailVerified(),
                savedAdmin.getCreatedAt()
        );
    }
}