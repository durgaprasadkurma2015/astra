
package com.astra.backend.auth;
import java.util.Map;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.time.LocalDateTime;
import com.astra.backend.audit.AuditService;
import com.astra.backend.auth.dto.AuthResponse;
import com.astra.backend.auth.dto.LoginRequest;
import com.astra.backend.auth.dto.RegisterRequest;
import com.astra.backend.user.AppUser;
import com.astra.backend.user.AppUserRepository;
import com.astra.backend.user.Role;
import com.astra.backend.user.CustomUserDetailsService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import com.astra.backend.auth.dto.ChangePasswordRequest;
import com.astra.backend.user.dto.UpdateProfileRequest;
import com.astra.backend.user.dto.UserProfileResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.dao.DataAccessException;



@Service
public class AuthService {

    private final AppUserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final CustomUserDetailsService userDetailsService;
    private final JwtService jwtService;
    private final RegistrationOtpRequestRepository otpRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final RegistrationOtpEmailService otpEmailService;
    private final SecureRandom secureRandom = new SecureRandom();
    private final AuditService auditService ;
    @Value("${app.password-reset.otp-hmac-secret}")
    private String otpHmacSecret;

    @Value("${app.registration-otp.expiration-seconds:300}")
    private long otpExpirationSeconds;

    @Value("${app.registration-otp.resend-cooldown-seconds:60}")
    private long resendCooldownSeconds;

    @Value("${app.registration-otp.max-attempts:5}")
    private int maxAttempts;

    public AuthService(
    	    AppUserRepository repository,
    	    PasswordEncoder passwordEncoder,
    	    AuthenticationManager authenticationManager,
    	    CustomUserDetailsService userDetailsService,
    	    JwtService jwtService,
    	    RegistrationOtpRequestRepository otpRepository,
    	    RefreshTokenRepository refreshTokenRepository,
    	    RegistrationOtpEmailService otpEmailService,
    	    AuditService auditService
    	) {
    	    this.repository = repository;
    	    this.passwordEncoder = passwordEncoder;
    	    this.authenticationManager = authenticationManager;
    	    this.userDetailsService = userDetailsService;
    	    this.jwtService = jwtService;
    	    this.otpRepository = otpRepository;
    	    this.refreshTokenRepository = refreshTokenRepository;
    	    this.otpEmailService = otpEmailService;
    	    this.auditService = auditService;
    	}
    @Transactional
    public String register(RegisterRequest request) {
        String email = normalizeEmail(request.email());

        if (repository.existsByEmail(email)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "An account with this email already exists. If you haven't verified it, request a new OTP."
            );
        }

        AppUser user = new AppUser();
        user.setFullName(request.fullName().trim());
        user.setEmail(email);
        user.setPasswordHash(passwordEncoder.encode(request.password()));
        user.setRole(Role.CUSTOMER);
        user.setEmailVerified(false);

        AppUser savedUser = repository.saveAndFlush(user);
        createAndSendOtp(savedUser);

        return "Registration started. Check your email for the verification code.";
    }

    @Transactional
    public boolean verifyRegistrationOtp(String rawEmail, String otp) {
        String email = normalizeEmail(rawEmail);

        AppUser user = repository.findByEmail(email).orElse(null);
        if (user == null || user.isEmailVerified()) {
            return false;
        }

        List<RegistrationOtpRequest> pending =
            otpRepository.findAllByUser_IdAndUsedAtIsNullOrderByCreatedAtDesc(
                user.getId()
            );

        if (pending.isEmpty()) {
            return false;
        }

        RegistrationOtpRequest request =
            otpRepository.findByIdForUpdate(pending.get(0).getId())
                .orElse(null);

        if (request == null || request.getUsedAt() != null) {
            return false;
        }

        LocalDateTime now = LocalDateTime.now(java.time.ZoneOffset.UTC);

        if (!request.getExpiresAt().isAfter(now)
                || request.getAttempts() >= maxAttempts) {
            request.setUsedAt(now);
            return false;
        }

        if (!constantTimeEquals(request.getOtpHash(), hashOtp(email, otp))) {
            request.incrementAttempts();
            if (request.getAttempts() >= maxAttempts) {
                request.setUsedAt(now);
            }
            // Return normally so the failed attempt is committed.
            return false;
        }

        request.setVerifiedAt(now);
        request.setUsedAt(now);
        user.setEmailVerified(true);
        repository.save(user);
        refreshTokenRepository.revokeAllForUser(
        	    user.getEmail(),
        	    LocalDateTime.now(java.time.ZoneOffset.UTC)
        	);
        return true;
    }

    @Transactional
    public String resendRegistrationOtp(String rawEmail) {
        String email = normalizeEmail(rawEmail);

        AppUser user = repository.findByEmail(email)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Unable to resend verification code. Check the email or register again."
            ));

        if (user.isEmailVerified()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "This email is already verified. Please log in."
            );
        }

        List<RegistrationOtpRequest> previous =
            otpRepository.findAllByUser_IdAndUsedAtIsNullOrderByCreatedAtDesc(
                user.getId()
            );

        if (!previous.isEmpty()) {
            LocalDateTime allowedAt = previous.get(0).getLastSentAt()
                .plusSeconds(resendCooldownSeconds);

            if (LocalDateTime.now(java.time.ZoneOffset.UTC).isBefore(allowedAt)) {
                throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Please wait before requesting another OTP."
                );
            }
        }

        createAndSendOtp(user);
        return "A new verification code has been sent if the email is eligible.";
    }

    private void createAndSendOtp(AppUser user) {
        LocalDateTime now = LocalDateTime.now(java.time.ZoneOffset.UTC);

        // Invalidate older unused codes when a new one is issued.
        List<RegistrationOtpRequest> previous =
            otpRepository.findAllByUser_IdAndUsedAtIsNullOrderByCreatedAtDesc(
                user.getId()
            );

        for (RegistrationOtpRequest old : previous) {
            old.setUsedAt(now);
        }

        String otp = String.format(
            Locale.ROOT, "%06d", secureRandom.nextInt(1_000_000)
        );

        RegistrationOtpRequest record = new RegistrationOtpRequest(
            user,
            hashOtp(user.getEmail(), otp),
            now.plusSeconds(otpExpirationSeconds),
            now
        );

        otpRepository.save(record);
        otpRepository.flush();

        // If SMTP sending fails, the transaction should roll back.
        otpEmailService.sendOtp(user.getEmail(), otp);
    }

    @Transactional
    public AuthSession login(LoginRequest request) {
        String email = normalizeEmail(request.email());

        // Check whether the user exists
        AppUser user = repository.findByEmail(email).orElse(null);

        if (user == null) {
            auditService.log(
                email,
                "LOGIN",
                "FAILURE",
                "Login failed: invalid credentials",
                null
            );

            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Invalid email or password"
            );
        }

        // Check whether email is verified
        if (!user.isEmailVerified()) {
    
            auditService.log(
                email,
                "LOGIN",
                "FAILURE",
                "Login failed: email not verified",
                null
            );

            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Please verify your email before logging in."
            );
        }

        // Authenticate credentials
        try {
            authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    email,
                    request.password()
                )
            );
        } catch (AuthenticationException ex) {
            auditService.log(
                email,
                "LOGIN",
                "FAILURE",
                "Login failed: invalid credentials",
                null
            );

            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Invalid email or password"
            );
        }

        // Record successful login
        UserDetails details =
            userDetailsService.loadUserByUsername(email);

        auditService.log(
            email,
            "LOGIN",
            "SUCCESS",
            "User logged in successfully",
            null
        );

        return createSession(user, details, request.rememberMe());
    }

    @Transactional
    public AuthSession refresh(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Refresh session is missing or expired");
        }
        String hash = hashRefreshToken(rawToken);
        RefreshToken stored = refreshTokenRepository.findActiveByHashForUpdate(hash)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Refresh session is invalid"));
        LocalDateTime now = LocalDateTime.now(java.time.ZoneOffset.UTC);
        stored.setRevokedAt(now);
        if (!stored.getExpiresAt().isAfter(now)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED,
                    "Refresh session has expired");
        }
        AppUser user = repository.findByEmail(stored.getUserEmail())
                .filter(AppUser::isActive)
                .filter(AppUser::isEmailVerified)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Account is not available"));
        UserDetails details = userDetailsService.loadUserByUsername(user.getEmail());
        return createSession(user, details, stored.isPersistent());
    }

    @Transactional
    public void revokeRefreshToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) return;
        refreshTokenRepository.findActiveByHashForUpdate(hashRefreshToken(rawToken))
                .ifPresent(token -> token.setRevokedAt(
                        LocalDateTime.now(java.time.ZoneOffset.UTC)));
    }

    private AuthSession createSession(AppUser user, UserDetails details,
            boolean persistent) {
        byte[] bytes = new byte[48];
        secureRandom.nextBytes(bytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
        int lifetimeHours = persistent ? 24 * 30 : 12;
        LocalDateTime expiresAt = LocalDateTime.now(java.time.ZoneOffset.UTC)
                .plusHours(lifetimeHours);
        refreshTokenRepository.save(new RefreshToken(user.getEmail(),
                hashRefreshToken(rawToken), persistent, expiresAt));
        return new AuthSession(createResponse(user, details), rawToken, persistent);
    }

    private String hashRefreshToken(String token) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(token.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (java.security.NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is unavailable", exception);
        }
    }

    public record AuthSession(AuthResponse response, String refreshToken,
            boolean persistent) { }

    private AuthResponse createResponse(AppUser user, UserDetails details) {
        return new AuthResponse(
            jwtService.generateToken(details),
            "Bearer",
            user.getId(),
            user.getFullName(),
            user.getEmail(),
            user.getRole().name()
        );
    }

    private String hashOtp(String email, String otp) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(
                Base64.getDecoder().decode(otpHmacSecret),
                "HmacSHA256"
            ));
            byte[] result = mac.doFinal(
                (email + ":" + otp).getBytes(StandardCharsets.UTF_8)
            );
            return HexFormat.of().formatHex(result);
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to hash OTP", ex);
        }
    }

    private boolean constantTimeEquals(String a, String b) {
        return MessageDigest.isEqual(
            a.getBytes(StandardCharsets.UTF_8),
            b.getBytes(StandardCharsets.UTF_8)
        );
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

@Transactional(readOnly = true)
public Map<String, Object> getCurrentUser(String authenticatedEmail) {
    String email = normalizeEmail(authenticatedEmail);

    AppUser user = repository.findByEmail(email)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.UNAUTHORIZED,
            "User not found"
        ));

    if (!user.isActive() || !user.isEmailVerified()) {
        throw new ResponseStatusException(
            HttpStatus.FORBIDDEN,
            "Account is inactive or email is not verified"
        );
    }

    return Map.of(
        "id", user.getId(),
        "fullName", user.getFullName(),
        "email", user.getEmail(),
        "role", user.getRole().name()
    );
}

@Transactional(readOnly = true)
public UserProfileResponse getProfile(String authenticatedEmail) {
    AppUser user = repository.findByEmail(
        normalizeEmail(authenticatedEmail)
    ).orElseThrow(() -> new ResponseStatusException(
        HttpStatus.UNAUTHORIZED, "User not found"
    ));

    return toProfileResponse(user);
}


@Transactional
public String changePassword(
        String authenticatedEmail,
        ChangePasswordRequest request) {

    String email = normalizeEmail(authenticatedEmail);

    AppUser user = repository.findByEmail(email)
        .orElseThrow(() -> new ResponseStatusException(
            HttpStatus.UNAUTHORIZED,
            "User not found"
        ));

    if (!user.isActive() || !user.isEmailVerified()) {
        throw new ResponseStatusException(
            HttpStatus.FORBIDDEN,
            "Account is inactive or email is not verified"
        );
    }

    if (!passwordEncoder.matches(
            request.currentPassword(),
            user.getPasswordHash())) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "Current password is incorrect"
        );
    }

    if (passwordEncoder.matches(
            request.newPassword(),
            user.getPasswordHash())) {
        throw new ResponseStatusException(
            HttpStatus.BAD_REQUEST,
            "New password must be different from current password"
        );
    }

    user.setPasswordHash(
        passwordEncoder.encode(request.newPassword())
    );

    repository.save(user);
    auditService.log(
    	    email,
    	    "CHANGE_PASSWORD",
    	    "SUCCESS",
    	    "Password changed successfully",
    	    null
    	);

    return "Password changed successfully";
}


@Transactional
public UserProfileResponse updateProfile(
    String authenticatedEmail,
    UpdateProfileRequest request
) {
    AppUser user = repository.findByEmail(
        normalizeEmail(authenticatedEmail)
    ).orElseThrow(() -> new ResponseStatusException(
        HttpStatus.UNAUTHORIZED, "User not found"
    ));

    if (!user.isActive() || !user.isEmailVerified()) {
        throw new ResponseStatusException(
            HttpStatus.FORBIDDEN, "Account is inactive or unverified"
        );
    }

    user.setFullName(request.fullName().trim());
    user.setDateOfBirth(request.dateOfBirth());
    user.setGender(blankToNull(request.gender()));
    user.setPhoneNumber(blankToNull(request.phoneNumber()));
    user.setAlternatePhoneNumber(blankToNull(request.alternatePhoneNumber()));
    user.setAddressLine1(blankToNull(request.addressLine1()));
    user.setAddressLine2(blankToNull(request.addressLine2()));
    user.setLandmark(blankToNull(request.landmark()));
    user.setCity(blankToNull(request.city()));
    user.setDistrict(blankToNull(request.district()));
    user.setState(blankToNull(request.state()));
    user.setCountry(blankToNull(request.country()));
    user.setPostalCode(blankToNull(request.postalCode()));
    user.setPreferredLanguage(blankToNull(request.preferredLanguage()));

    return toProfileResponse(repository.save(user));
}

private UserProfileResponse toProfileResponse(AppUser user) {
    return new UserProfileResponse(
        user.getId(),
        user.getFullName(),
        user.getEmail(),
        user.getDateOfBirth(),
        user.getGender(),
        user.getPhoneNumber(),
        user.getAlternatePhoneNumber(),
        user.getAddressLine1(),
        user.getAddressLine2(),
        user.getLandmark(),
        user.getCity(),
        user.getDistrict(),
        user.getState(),
        user.getCountry(),
        user.getPostalCode(),
        user.getPreferredLanguage(),
        user.getRole().name(),
        user.isEmailVerified(),
        user.getCreatedAt()
    );
}

private String blankToNull(String value) {
    return value == null || value.isBlank() ? null : value.trim();
}


}
