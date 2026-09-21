package com.astra.auth.service;

import com.astra.auth.dto.ForgotPasswordRequest;
import com.astra.auth.dto.GoogleLoginRequest;
import com.astra.auth.dto.LoginRequest;
import com.astra.auth.dto.RegisterRequest;
import com.astra.auth.dto.ResetPasswordRequest;
import com.astra.auth.dto.VerifyOtpRequest;
import com.astra.dto.AuthResponse;
import com.astra.dto.MessageResponse;
import com.astra.entity.Role;
import com.astra.entity.User;
import com.astra.exception.ApiException;
import com.astra.repository.UserRepository;
import com.astra.service.OtpService;
import com.astra.service.PasswordResetService;
import com.astra.service.TokenService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;

@Service
public class AuthService {

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final AuthenticationManager authenticationManager;
    private final OtpService otpService;
    private final TokenService tokenService;
    private final PasswordResetService passwordResetService;
    private final RestClient restClient;
    private final String googleClientId;

    public AuthService(
            UserRepository users,
            PasswordEncoder encoder,
            AuthenticationManager authenticationManager,
            OtpService otpService,
            TokenService tokenService,
            PasswordResetService passwordResetService,
            @Value("${astra.google.client-id:}") String googleClientId) {

        this.users = users;
        this.encoder = encoder;
        this.authenticationManager = authenticationManager;
        this.otpService = otpService;
        this.tokenService = tokenService;
        this.passwordResetService = passwordResetService;
        this.googleClientId = googleClientId;
        this.restClient = RestClient.builder().build();
    }

    // =========================================================
    // REGISTER
    // =========================================================

    public MessageResponse register(RegisterRequest request) {

        String email = normalizeEmail(request.email());

        if (users.existsByEmailIgnoreCase(email)) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "An account with this email already exists."
            );
        }

        String phone = normalizePhone(request.phone());

        if (phone != null && users.existsByPhone(phone)) {
            throw new ApiException(
                    HttpStatus.CONFLICT,
                    "An account with this phone number already exists."
            );
        }

        User user = User.builder()
                .name(request.name().trim())
                .email(email)
                .phone(phone)
                .password(encoder.encode(request.password()))
                .role(Role.RoleName.CUSTOMER)
                .enabled(true)
                .emailVerified(false)
                .phoneVerified(false)
                .build();

        users.save(user);

        otpService.createEmailOtp(email);

        return new MessageResponse(
                "Registration successful. Please verify the OTP sent to your email."
        );
    }

    // =========================================================
    // LOGIN
    // =========================================================

    public AuthResponse login(LoginRequest request) {

        String email = normalizeEmail(request.email());

        User user = users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid email or password."
                ));

        if (!user.isEmailVerified()) {
            throw new ApiException(
                    HttpStatus.FORBIDDEN,
                    "Please verify your email OTP before signing in."
            );
        }

        try {

            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            email,
                            request.password()
                    )
            );

        } catch (RuntimeException ex) {

            throw new ApiException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid email or password."
            );
        }

        return session(user);
    }

    // =========================================================
    // VERIFY EMAIL OTP
    // =========================================================

    public AuthResponse verifyEmailOtp(VerifyOtpRequest request) {

        String email = normalizeEmail(request.email());

        User user = users.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "User not found."
                ));

        otpService.verifyEmail(
                email,
                request.otp()
        );

        user.setEmailVerified(true);

        users.save(user);

        return session(user);
    }

    // =========================================================
    // SEND SMS OTP
    // =========================================================

    public MessageResponse sendSmsOtp(String phone) {

        String normalized = normalizePhone(phone);

        if (normalized == null) {
            throw new ApiException(
                    HttpStatus.BAD_REQUEST,
                    "Phone number is required."
            );
        }

        if (!users.existsByPhone(normalized)) {
            throw new ApiException(
                    HttpStatus.NOT_FOUND,
                    "No Astra account is registered with this phone number."
            );
        }

        otpService.createSmsOtp(normalized);

        return new MessageResponse(
                "SMS OTP sent successfully."
        );
    }

    // =========================================================
    // VERIFY SMS OTP
    // =========================================================

    public AuthResponse verifySmsOtp(
            String phone,
            String otp) {

        String normalized = normalizePhone(phone);

        User user = users.findByPhone(normalized)
                .orElseThrow(() -> new ApiException(
                        HttpStatus.NOT_FOUND,
                        "User not found."
                ));

        otpService.verifyPhone(
                normalized,
                otp
        );

        user.setPhoneVerified(true);

        users.save(user);

        return session(user);
    }

    // =========================================================
    // REFRESH TOKEN
    // =========================================================

    public AuthResponse refresh(String rawRefreshToken) {

        User user = tokenService.validateRefreshToken(
                rawRefreshToken
        );

        tokenService.revoke(
                rawRefreshToken
        );

        return session(user);
    }

    // =========================================================
    // LOGOUT
    // =========================================================

    public MessageResponse logout(String rawRefreshToken) {

        tokenService.revoke(
                rawRefreshToken
        );

        return new MessageResponse(
                "Logged out successfully."
        );
    }

    // =========================================================
    // FORGOT PASSWORD
    // =========================================================

    public MessageResponse forgotPassword(
            ForgotPasswordRequest request) {

        passwordResetService.request(
                normalizeEmail(request.email())
        );

        return new MessageResponse(
                "If an account exists for that email, password reset instructions have been generated."
        );
    }

    // =========================================================
    // RESET PASSWORD
    // =========================================================

    public MessageResponse resetPassword(
            ResetPasswordRequest request) {

        passwordResetService.reset(
                request.token(),
                request.newPassword()
        );

        return new MessageResponse(
                "Password reset successfully. You can now sign in."
        );
    }

    // =========================================================
    // GOOGLE LOGIN
    // =========================================================

    public AuthResponse googleLogin(
            GoogleLoginRequest request) {

        if (googleClientId == null
                || googleClientId.isBlank()) {

            throw new ApiException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "Google sign-in is not configured."
            );
        }

        Map<?, ?> claims;

        try {

            String encodedToken = URLEncoder.encode(
                    request.idToken(),
                    StandardCharsets.UTF_8
            );

            claims = restClient.get()
                    .uri(
                            "https://oauth2.googleapis.com/tokeninfo?id_token="
                                    + encodedToken
                    )
                    .retrieve()
                    .body(Map.class);

        } catch (Exception ex) {

            throw new ApiException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid Google ID token."
            );
        }

        if (claims == null) {

            throw new ApiException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid Google ID token."
            );
        }

        String audience = String.valueOf(
                claims.get("aud")
        );

        String issuer = String.valueOf(
                claims.get("iss")
        );

        String subject = String.valueOf(
                claims.get("sub")
        );

        String email = String.valueOf(
                claims.get("email")
        );

        Object nameValue = claims.get("name");

        String name = nameValue != null
                ? String.valueOf(nameValue)
                : "Astra User";

        String emailVerified = String.valueOf(
                claims.get("email_verified")
        );

        boolean validIssuer =
                "accounts.google.com".equals(issuer)
                        || "https://accounts.google.com".equals(issuer);

        if (!googleClientId.equals(audience)
                || !validIssuer
                || subject.isBlank()
                || email.isBlank()
                || !"true".equalsIgnoreCase(emailVerified)) {

            throw new ApiException(
                    HttpStatus.UNAUTHORIZED,
                    "Google account verification failed."
            );
        }

        User user = users.findByGoogleSubject(subject)
                .orElseGet(() ->
                        users.findByEmailIgnoreCase(
                                normalizeEmail(email)
                        ).orElse(null)
                );

        if (user == null) {

            user = User.builder()
                    .name(name)
                    .email(normalizeEmail(email))
                    .googleSubject(subject)
                    .role(Role.RoleName.CUSTOMER)
                    .enabled(true)
                    .emailVerified(true)
                    .phoneVerified(false)
                    .build();

        } else {

            user.setGoogleSubject(subject);
            user.setEmailVerified(true);
        }

        users.save(user);

        return session(user);
    }

    // =========================================================
    // CREATE SESSION
    // =========================================================

    private AuthResponse session(User user) {

        String accessToken =
                tokenService.issueAccessToken(user);

        String refreshToken =
                tokenService.issueRefreshToken(user);

        return new AuthResponse(
                accessToken,
                refreshToken,
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole().name()
        );
    }

    // =========================================================
    // NORMALIZE EMAIL
    // =========================================================

    public static String normalizeEmail(String email) {

        if (email == null) {
            return null;
        }

        return email
                .trim()
                .toLowerCase();
    }

    // =========================================================
    // NORMALIZE PHONE
    // =========================================================

    public static String normalizePhone(String phone) {

        if (phone == null || phone.isBlank()) {
            return null;
        }

        return phone
                .trim()
                .replaceAll("[\\s()-]", "");
    }
}