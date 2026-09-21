package com.astra.user.service;

import com.astra.dto.UpdateProfileRequest;
import com.astra.dto.UserProfileResponse;
import com.astra.entity.User;
import com.astra.repository.UserRepository;
import com.astra.user.dto.ChangePasswordRequest;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
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
                        new RuntimeException(
                                "User not found: " + userId
                        ));
    }

    public UserProfileResponse getProfile(Long userId) {

        User user = getUser(userId);

        return toProfileResponse(user);
    }

    public UserProfileResponse updateProfile(
            Long userId,
            UpdateProfileRequest request
    ) {

        User user = getUser(userId);

        user.setName(request.name());
        user.setPhone(request.phone());

        User savedUser = userRepository.save(user);

        return toProfileResponse(savedUser);
    }

    public void changePassword(
            Long userId,
            ChangePasswordRequest request
    ) {

        User user = getUser(userId);

        if (!passwordEncoder.matches(
                request.currentPassword(),
                user.getPassword()
        )) {

            throw new RuntimeException(
                    "Current password is incorrect"
            );
        }

        if (passwordEncoder.matches(
                request.newPassword(),
                user.getPassword()
        )) {

            throw new RuntimeException(
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
