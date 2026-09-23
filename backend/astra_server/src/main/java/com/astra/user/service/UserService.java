package com.astra.user.service;

import com.astra.dto.UpdateProfileRequest;
import com.astra.dto.UserProfileResponse;
import com.astra.entity.User;
import com.astra.exception.BadRequestException;
import com.astra.exception.UserNotFoundException;
import com.astra.repository.UserRepository;
import com.astra.user.dto.ChangePasswordRequest;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Objects;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User getUser(Long userId) {

        return userRepository.findById(userId)
                .orElseThrow(() ->
                        new UserNotFoundException(
                                "User not found: " + userId
                        ));
    }

    @Transactional(readOnly = true)
    public UserProfileResponse getProfile(Long userId) {

        User user = getUser(userId);

        return toProfileResponse(user);
    }

    public UserProfileResponse updateProfile(
            Long userId,
            UpdateProfileRequest request
    ) {

        User user = getUser(userId);

        String name = request.name() == null
                ? null
                : request.name().trim();

        if (name == null || name.isBlank()) {
            throw new BadRequestException(
                    "Name is required"
            );
        }

        String phone = request.phone();

        if (phone != null) {
            phone = phone.trim();

            if (phone.isBlank()) {
                phone = null;
            }
        }

        String oldPhone = user.getPhone();

        /*
         * Prevent the same phone number from
         * being assigned to another user.
         */
        if (phone != null &&
                !phone.equals(oldPhone) &&
                userRepository.existsByPhone(phone)) {

            throw new BadRequestException(
                    "Phone number is already registered"
            );
        }

        user.setName(name);
        user.setPhone(phone);

        /*
         * Changing the phone number invalidates
         * the previous phone verification.
         */
        if (!Objects.equals(oldPhone, phone)) {
            user.setPhoneVerified(false);
        }

        User savedUser = userRepository.save(user);

        return toProfileResponse(savedUser);
    }

    public void changePassword(
            Long userId,
            ChangePasswordRequest request
    ) {

        User user = getUser(userId);

        /*
         * Google-only accounts may not have
         * a local password.
         */
        if (user.getPassword() == null ||
                user.getPassword().isBlank()) {

            throw new BadRequestException(
                    "Password change is not available for this account"
            );
        }

        if (!passwordEncoder.matches(
                request.currentPassword(),
                user.getPassword()
        )) {

            throw new BadRequestException(
                    "Current password is incorrect"
            );
        }

        if (passwordEncoder.matches(
                request.newPassword(),
                user.getPassword()
        )) {

            throw new BadRequestException(
                    "New password must be different from current password"
            );
        }

        user.setPassword(
                passwordEncoder.encode(
                        request.newPassword()
                )
        );

        userRepository.save(user);
    }

    private UserProfileResponse toProfileResponse(
            User user
    ) {

        return new UserProfileResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole().name(),
                user.isEmailVerified(),
                user.isPhoneVerified()
        );
    }
}
