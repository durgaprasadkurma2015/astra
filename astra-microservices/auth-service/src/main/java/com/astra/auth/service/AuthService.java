package com.astra.auth.service;

import com.astra.auth.dto.RegisterRequest;
import com.astra.auth.dto.VerifyRegistrationOtpRequest;
import com.astra.auth.dto.ResendRegistrationOtpRequest;
import com.astra.auth.entity.AppUser;
import com.astra.auth.entity.RegistrationOtpRequest;
import com.astra.auth.entity.Role;
import com.astra.auth.repository.AppUserRepository;
import com.astra.auth.repository.RegistrationOtpRequestRepository;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;


import com.astra.auth.dto.LoginRequest;
import com.astra.auth.dto.LoginResponse;
import com.astra.auth.security.CustomUserDetails;
import com.astra.auth.security.JwtService;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;

@Service
public class AuthService {

    private final AppUserRepository userRepository;
    private final RegistrationOtpRequestRepository otpRepository;
    private final PasswordEncoder passwordEncoder;
    private final RegistrationOtpEmailService emailService;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    private final SecureRandom secureRandom = new SecureRandom();

    @Value("${app.registration-otp.hmac-secret}")
    private String otpHmacSecret;

    @Value("${app.registration-otp.expiration-seconds:300}")
    private long otpExpirationSeconds;

    @Value("${app.registration-otp.resend-cooldown-seconds:60}")
    private long otpResendCooldownSeconds;

    @Value("${app.registration-otp.max-attempts:5}")
    private int otpMaxAttempts;

    public AuthService(
            AppUserRepository userRepository,
            RegistrationOtpRequestRepository otpRepository,
            PasswordEncoder passwordEncoder,
            RegistrationOtpEmailService emailService,
            AuthenticationManager authenticationManager,
            JwtService jwtService) {

        this.userRepository = userRepository;
        this.otpRepository = otpRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    // ------------------------------------------------
    // 1. REGISTER
    // ------------------------------------------------

    @Transactional
    public String register(RegisterRequest request) {

        String email = normalizeEmail(request.email());

        if (userRepository.existsByEmail(email)) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "An account with this email already exists."
            );
        }

        AppUser user = new AppUser();
        user.setFullName(request.fullName().trim());
        user.setEmail(email);
        user.setPasswordHash(
                passwordEncoder.encode(request.password())
        );

        // Public registration always creates CUSTOMER.
        user.setRole(Role.CUSTOMER);
        user.setEmailVerified(false);

        try {
            AppUser savedUser = userRepository.saveAndFlush(user);
            createAndSendOtp(savedUser);

        } catch (DataIntegrityViolationException ex) {
            throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "An account with this email already exists."
            );
        }

        return "Registration started. Check your email for the verification OTP.";
    }

    // ------------------------------------------------
    // 2. VERIFY REGISTRATION OTP
    // ------------------------------------------------

    @Transactional(noRollbackFor = ResponseStatusException.class)
    public String verifyRegistrationOtp(
            VerifyRegistrationOtpRequest request) {

        String email = normalizeEmail(request.email());

        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid email or OTP."
                ));

        if (user.isEmailVerified()) {
            return "Email is already verified.";
        }

        RegistrationOtpRequest otpRequest =
                otpRepository
                        .findTopByUser_IdAndUsedAtIsNullOrderByCreatedAtDesc(
                                user.getId()
                        )
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "No active OTP found. Please request a new OTP."
                        ));

        LocalDateTime now = LocalDateTime.now();

        // Check whether the OTP has expired.
        if (!otpRequest.getExpiresAt().isAfter(now)) {

            otpRequest.setUsedAt(now);
            otpRepository.saveAndFlush(otpRequest);

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "OTP has expired. Please request a new OTP."
            );
        }

        // Check maximum attempts.
        if (otpRequest.getAttempts() >= otpMaxAttempts) {

            otpRequest.setUsedAt(now);
            otpRepository.saveAndFlush(otpRequest);

            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Maximum OTP attempts exceeded. Please request a new OTP."
            );
        }

        String suppliedOtpHash = hashOtp(email, request.otp());

        boolean validOtp = MessageDigest.isEqual(
                HexFormat.of().parseHex(otpRequest.getOtpHash()),
                HexFormat.of().parseHex(suppliedOtpHash)
        );

        if (!validOtp) {

            otpRequest.incrementAttempts();

            if (otpRequest.getAttempts() >= otpMaxAttempts) {
                otpRequest.setUsedAt(now);
            }

            otpRepository.saveAndFlush(otpRequest);

            if (otpRequest.getAttempts() >= otpMaxAttempts) {
                throw new ResponseStatusException(
                        HttpStatus.TOO_MANY_REQUESTS,
                        "Maximum OTP attempts exceeded. Please request a new OTP."
                );
            }

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid OTP. Attempts remaining: "
                            + (otpMaxAttempts - otpRequest.getAttempts())
            );
        }

        // OTP is correct. Mark it verified and consumed.
        otpRequest.setVerifiedAt(now);
        otpRequest.setUsedAt(now);

        user.setEmailVerified(true);

        otpRepository.save(otpRequest);
        userRepository.save(user);

        return "Email verified successfully. You can now log in.";
    }

    // ------------------------------------------------
    // 3. RESEND REGISTRATION OTP
    // ------------------------------------------------

    @Transactional
    public String resendRegistrationOtp(
            ResendRegistrationOtpRequest request) {

        String email = normalizeEmail(request.email());

        AppUser user = userRepository.findByEmail(email)
                .orElse(null);

        // Generic response prevents revealing whether an account exists.
        if (user == null || user.isEmailVerified()) {
            return "If the account is eligible, a verification OTP will be sent.";
        }

        LocalDateTime now = LocalDateTime.now();

        // Check the latest OTP, including previously consumed OTPs.
        RegistrationOtpRequest latestOtp =
                otpRepository
                        .findTopByUser_IdOrderByCreatedAtDesc(user.getId())
                        .orElse(null);

        if (latestOtp != null) {

            long secondsSinceLastSend = Duration.between(
                    latestOtp.getLastSentAt(),
                    now
            ).getSeconds();

            if (secondsSinceLastSend < otpResendCooldownSeconds) {

                long remainingSeconds =
                        otpResendCooldownSeconds - secondsSinceLastSend;

                throw new ResponseStatusException(
                        HttpStatus.TOO_MANY_REQUESTS,
                        "Please wait " + remainingSeconds
                                + " seconds before requesting another OTP."
                );
            }
        }

        createAndSendOtp(user);

        return "A new verification OTP has been sent to your email.";
    }

    // ------------------------------------------------
    // 4. CREATE AND SEND OTP
    // ------------------------------------------------

    private void createAndSendOtp(AppUser user) {

        LocalDateTime now = LocalDateTime.now();

        // Invalidate previous unused OTPs.
        List<RegistrationOtpRequest> previous =
                otpRepository
                        .findAllByUser_IdAndUsedAtIsNullOrderByCreatedAtDesc(
                                user.getId()
                        );

        for (RegistrationOtpRequest oldOtp : previous) {
            oldOtp.setUsedAt(now);
        }

        String otp = String.format(
                Locale.ROOT,
                "%06d",
                secureRandom.nextInt(1_000_000)
        );

        String otpHash = hashOtp(user.getEmail(), otp);

        RegistrationOtpRequest otpRequest =
                new RegistrationOtpRequest(
                        user,
                        otpHash,
                        now.plusSeconds(otpExpirationSeconds),
                        now
                );

        otpRepository.saveAndFlush(otpRequest);

        emailService.sendOtp(user.getEmail(), otp);
    }

    // ------------------------------------------------
    // 5. HASH OTP
    // ------------------------------------------------

    private String hashOtp(String email, String otp) {

        try {
            Mac mac = Mac.getInstance("HmacSHA256");

            byte[] secretBytes =
                    Base64.getDecoder().decode(otpHmacSecret);

            mac.init(
                    new SecretKeySpec(secretBytes, "HmacSHA256")
            );

            byte[] result = mac.doFinal(
                    (email + ":" + otp)
                            .getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(result);

        } catch (Exception ex) {
            throw new IllegalStateException(
                    "Unable to hash registration OTP", ex
            );
        }
    }

    // ------------------------------------------------
    // 6. NORMALIZE EMAIL
    // ------------------------------------------------

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }
    
    public LoginResponse login(LoginRequest request) {

        String email = normalizeEmail(request.email());

        try {
            var authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    email,
                    request.password()
                )
            );

            CustomUserDetails user =
                (CustomUserDetails) authentication.getPrincipal();

            String token = jwtService.generateToken(user);

            return new LoginResponse(
                token,
                "Bearer",
                jwtService.getExpirationMs() / 1000,
                user.getEmail(),
                user.getRole()
            );

        } catch (DisabledException ex) {

            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Your account is inactive or email verification is pending."
            );

        } catch (BadCredentialsException ex) {

            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Invalid email or password."
            );
        }
    }
}