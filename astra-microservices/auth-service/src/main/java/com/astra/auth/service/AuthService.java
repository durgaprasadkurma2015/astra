
package com.astra.auth.service;

import com.astra.auth.dto.RegisterRequest;
import com.astra.auth.dto.VerifyRegistrationOtpRequest;
import com.astra.auth.dto.ResendRegistrationOtpRequest;
import com.astra.auth.dto.ResetPasswordRequest;
import com.astra.auth.dto.ForgotPasswordRequest;
import com.astra.auth.dto.LoginRequest;
import com.astra.auth.dto.LoginResponse;
import com.astra.auth.dto.ChangePasswordRequest;
import com.astra.auth.entity.AppUser;
import com.astra.auth.entity.RegistrationOtpRequest;
import com.astra.auth.entity.PasswordResetOtpRequest;
import com.astra.auth.entity.Role;

import com.astra.auth.repository.AppUserRepository;
import com.astra.auth.repository.RegistrationOtpRequestRepository;
import com.astra.auth.repository.PasswordResetOtpRequestRepository;

import com.astra.auth.security.CustomUserDetails;
import com.astra.auth.security.JwtService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import com.astra.auth.entity.RevokedToken;
import com.astra.auth.repository.RevokedTokenRepository;

import java.time.ZoneId;
import java.util.Date;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.HexFormat;
import java.util.List;
import java.util.Locale;

@Service
public class AuthService {

    private final AppUserRepository userRepository;
    private final RegistrationOtpRequestRepository otpRepository;
    private final PasswordResetOtpRequestRepository passwordResetOtpRepository;
    private final RevokedTokenRepository revokedTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final RegistrationOtpEmailService emailService;
    private final PasswordResetOtpEmailService passwordResetEmailService;

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    private final SecureRandom secureRandom = new SecureRandom();

    // Registration OTP configuration

    @Value("${app.registration-otp.hmac-secret}")
    private String otpHmacSecret;

    @Value("${app.registration-otp.expiration-seconds:300}")
    private long otpExpirationSeconds;

    @Value("${app.registration-otp.resend-cooldown-seconds:60}")
    private long otpResendCooldownSeconds;

    @Value("${app.registration-otp.max-attempts:5}")
    private int otpMaxAttempts;

    // Password reset OTP configuration

    @Value("${app.password-reset-otp.hmac-secret:${app.registration-otp.hmac-secret}}")
    private String passwordResetOtpHmacSecret;

    @Value("${app.password-reset-otp.expiration-seconds:300}")
    private long passwordResetOtpExpirationSeconds;

    @Value("${app.password-reset-otp.resend-cooldown-seconds:60}")
    private long passwordResetOtpResendCooldownSeconds;

    @Value("${app.password-reset-otp.max-attempts:5}")
    private int passwordResetOtpMaxAttempts;

    // Constructor injection

    public AuthService(
            AppUserRepository userRepository,
            RegistrationOtpRequestRepository otpRepository,
            PasswordResetOtpRequestRepository passwordResetOtpRepository,
            PasswordEncoder passwordEncoder,
            RegistrationOtpEmailService emailService,
            PasswordResetOtpEmailService passwordResetEmailService,
            AuthenticationManager authenticationManager,
            RevokedTokenRepository revokedTokenRepository,
            JwtService jwtService) {

        this.userRepository = userRepository;
        this.otpRepository = otpRepository;
        this.passwordResetOtpRepository = passwordResetOtpRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.passwordResetEmailService = passwordResetEmailService;
        this.authenticationManager = authenticationManager;
        this.revokedTokenRepository = revokedTokenRepository;
        this.jwtService = jwtService;
    }
    
 // ------------------------------------------------
 // LOGOUT - REVOKE JWT TOKEN
 // ------------------------------------------------

 @Transactional
 public String logout(String token) {

     String tokenId = jwtService.extractTokenId(token);

     Date expiration = jwtService.extractExpiration(token);

     if (tokenId == null || tokenId.isBlank()) {
         throw new ResponseStatusException(
                 HttpStatus.UNAUTHORIZED,
                 "Invalid token"
         );
     }

     if (expiration == null || !expiration.after(new Date())) {
         throw new ResponseStatusException(
                 HttpStatus.UNAUTHORIZED,
                 "Token has expired"
         );
     }

     // Avoid duplicate token records
     if (revokedTokenRepository.existsByTokenId(tokenId)) {
         return "User already logged out";
     }

     LocalDateTime expiresAt = LocalDateTime.ofInstant(
             expiration.toInstant(),
             ZoneId.systemDefault()
     );

     RevokedToken revokedToken = new RevokedToken(
             tokenId,
             expiresAt,
             LocalDateTime.now()
     );

     revokedTokenRepository.save(revokedToken);

     return "Logout successful";
 }
    
 // ------------------------------------------------
 // CHANGE PASSWORD
 // ------------------------------------------------

 @Transactional
 public String changePassword(
         ChangePasswordRequest request,
         CustomUserDetails currentUser) {

     if (currentUser == null) {
         throw new ResponseStatusException(
                 HttpStatus.UNAUTHORIZED,
                 "Authentication required"
         );
     }

     AppUser user = userRepository.findByEmail(
             normalizeEmail(currentUser.getEmail())
     ).orElseThrow(() -> new ResponseStatusException(
             HttpStatus.NOT_FOUND,
             "User not found"
     ));

     // Verify current password
     if (!passwordEncoder.matches(
             request.getCurrentPassword(),
             user.getPasswordHash())) {

         throw new ResponseStatusException(
                 HttpStatus.BAD_REQUEST,
                 "Current password is incorrect"
         );
     }

     // Prevent reusing the current password
     if (passwordEncoder.matches(
             request.getNewPassword(),
             user.getPasswordHash())) {

         throw new ResponseStatusException(
                 HttpStatus.BAD_REQUEST,
                 "New password must be different from current password"
         );
     }

     // Encode and save new password
     user.setPasswordHash(
             passwordEncoder.encode(request.getNewPassword())
     );

     userRepository.save(user);

     return "Password changed successfully";
 }

    // ------------------------------------------------
    // 1. RESET PASSWORD
    // ------------------------------------------------

    @Transactional(noRollbackFor = ResponseStatusException.class)
    public String resetPassword(ResetPasswordRequest request) {

        String email = normalizeEmail(request.getEmail());

        AppUser user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "Invalid or expired OTP"
                ));

        PasswordResetOtpRequest otpRequest =
                passwordResetOtpRepository
                        .findTopByUser_IdAndUsedAtIsNullOrderByCreatedAtDesc(
                                user.getId()
                        )
                        .orElseThrow(() -> new ResponseStatusException(
                                HttpStatus.BAD_REQUEST,
                                "Invalid or expired OTP"
                        ));

        LocalDateTime now = LocalDateTime.now();

        // Check OTP expiration

        if (!otpRequest.getExpiresAt().isAfter(now)) {

            otpRequest.setUsedAt(now);
            passwordResetOtpRepository.save(otpRequest);

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "OTP has expired"
            );
        }

        // Check maximum attempts

        if (otpRequest.getAttempts() >= passwordResetOtpMaxAttempts) {

            otpRequest.setUsedAt(now);
            passwordResetOtpRepository.save(otpRequest);

            throw new ResponseStatusException(
                    HttpStatus.TOO_MANY_REQUESTS,
                    "Maximum OTP attempts exceeded"
            );
        }

        // Verify OTP

        String submittedHash =
                hashPasswordResetOtp(email, request.getOtp());

        boolean validOtp = MessageDigest.isEqual(
                submittedHash.getBytes(StandardCharsets.UTF_8),
                otpRequest.getOtpHash().getBytes(StandardCharsets.UTF_8)
        );

        if (!validOtp) {

            otpRequest.incrementAttempts();

            if (otpRequest.getAttempts() >= passwordResetOtpMaxAttempts) {
                otpRequest.setUsedAt(now);
            }

            passwordResetOtpRepository.save(otpRequest);

            if (otpRequest.getAttempts() >= passwordResetOtpMaxAttempts) {
                throw new ResponseStatusException(
                        HttpStatus.TOO_MANY_REQUESTS,
                        "Maximum OTP attempts exceeded. Request a new OTP."
                );
            }

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid OTP. Attempts remaining: "
                            + (passwordResetOtpMaxAttempts
                            - otpRequest.getAttempts())
            );
        }

        // Update password

        user.setPasswordHash(
                passwordEncoder.encode(request.getNewPassword())
        );

        userRepository.save(user);

        // Mark OTP as used

        otpRequest.setUsedAt(now);
        passwordResetOtpRepository.save(otpRequest);

        return "Password reset successfully";
    }

    // ------------------------------------------------
    // 2. FORGOT PASSWORD
    // ------------------------------------------------

    @Transactional
    public String forgotPassword(ForgotPasswordRequest request) {

        String email = normalizeEmail(request.email());

        AppUser user = userRepository.findByEmail(email)
                .orElse(null);

        // Generic response prevents account enumeration.

        if (user == null || !user.isActive() || !user.isEmailVerified()) {
            return "If the account is eligible, a password reset OTP will be sent.";
        }

        LocalDateTime now = LocalDateTime.now();

        // Check latest password reset OTP.

        PasswordResetOtpRequest latestOtp =
                passwordResetOtpRepository
                        .findTopByUser_IdOrderByCreatedAtDesc(user.getId())
                        .orElse(null);

        // Enforce resend cooldown.

        if (latestOtp != null) {

            long secondsSinceLastSend = Duration.between(
                    latestOtp.getLastSentAt(),
                    now
            ).getSeconds();

            if (secondsSinceLastSend < passwordResetOtpResendCooldownSeconds) {
                return "If the account is eligible, a password reset OTP will be sent.";
            }
        }

        createAndSendPasswordResetOtp(user);

        return "If the account is eligible, a password reset OTP will be sent.";
    }

    // ------------------------------------------------
    // 3. REGISTER
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
    // 4. VERIFY REGISTRATION OTP
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

        // Check OTP expiration.

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

        // OTP correct. Mark verified and consumed.

        otpRequest.setVerifiedAt(now);
        otpRequest.setUsedAt(now);

        user.setEmailVerified(true);

        otpRepository.save(otpRequest);
        userRepository.save(user);

        return "Email verified successfully. You can now log in.";
    }

    // ------------------------------------------------
    // 5. RESEND REGISTRATION OTP
    // ------------------------------------------------

    @Transactional
    public String resendRegistrationOtp(
            ResendRegistrationOtpRequest request) {

        String email = normalizeEmail(request.email());

        AppUser user = userRepository.findByEmail(email)
                .orElse(null);

        if (user == null || user.isEmailVerified()) {
            return "If the account is eligible, a verification OTP will be sent.";
        }

        LocalDateTime now = LocalDateTime.now();

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
    // 6. CREATE AND SEND REGISTRATION OTP
    // ------------------------------------------------

    private void createAndSendOtp(AppUser user) {

        LocalDateTime now = LocalDateTime.now();

        List<RegistrationOtpRequest> previous =
                otpRepository
                        .findAllByUser_IdAndUsedAtIsNullOrderByCreatedAtDesc(
                                user.getId()
                        );

        // Invalidate previous unused OTPs.

        for (RegistrationOtpRequest oldOtp : previous) {
            oldOtp.setUsedAt(now);
        }

        otpRepository.saveAll(previous);

        // Generate secure six-digit OTP.

        String otp = String.format(
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

        otpRepository.save(otpRequest);

        emailService.sendOtp(user.getEmail(), otp);
    }

    // ------------------------------------------------
    // 7. CREATE AND SEND PASSWORD RESET OTP
    // ------------------------------------------------

    private void createAndSendPasswordResetOtp(AppUser user) {

        LocalDateTime now = LocalDateTime.now();

        List<PasswordResetOtpRequest> previous =
                passwordResetOtpRepository
                        .findAllByUser_IdAndUsedAtIsNullOrderByCreatedAtDesc(
                                user.getId()
                        );

        // Invalidate previous unused password reset OTPs.

        for (PasswordResetOtpRequest oldOtp : previous) {
            oldOtp.setUsedAt(now);
        }

        passwordResetOtpRepository.saveAll(previous);

        // Generate secure six-digit OTP.

        String otp = String.format(
                "%06d",
                secureRandom.nextInt(1_000_000)
        );

        String otpHash = hashPasswordResetOtp(
                user.getEmail(),
                otp
        );

        PasswordResetOtpRequest otpRequest =
                new PasswordResetOtpRequest(
                        user,
                        otpHash,
                        now.plusSeconds(passwordResetOtpExpirationSeconds),
                        now
                );

        passwordResetOtpRepository.save(otpRequest);

        // Send password reset OTP email.

        passwordResetEmailService.sendOtp(
                user.getEmail(),
                otp
        );
    }

    // ------------------------------------------------
    // 8. HASH REGISTRATION OTP
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
    // 9. HASH PASSWORD RESET OTP
    // ------------------------------------------------

    private String hashPasswordResetOtp(String email, String otp) {

        try {

            Mac mac = Mac.getInstance("HmacSHA256");

            byte[] secretBytes =
                    Base64.getDecoder().decode(passwordResetOtpHmacSecret);

            mac.init(
                    new SecretKeySpec(secretBytes, "HmacSHA256")
            );

            // Domain separation.

            byte[] result = mac.doFinal(
                    ("PASSWORD_RESET:" + email + ":" + otp)
                            .getBytes(StandardCharsets.UTF_8)
            );

            return HexFormat.of().formatHex(result);

        } catch (Exception ex) {

            throw new IllegalStateException(
                    "Unable to hash password reset OTP", ex
            );
        }
    }

    // ------------------------------------------------
    // 10. NORMALIZE EMAIL
    // ------------------------------------------------

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    // ------------------------------------------------
    // 11. LOGIN
    // ------------------------------------------------

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
