package com.astra.admin.service;

import com.astra.admin.dto.AdminUserResponse;
import com.astra.entity.User;
import com.astra.repository.UserRepository;

import jakarta.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class AdminUserService {

private final UserRepository userRepository;

public AdminUserService(
        UserRepository userRepository
) {
    this.userRepository = userRepository;
}

public Page<AdminUserResponse> getUsers(
        Pageable pageable
) {

    return userRepository
            .findAll(pageable)
            .map(this::toResponse);
}

public AdminUserResponse getUser(
        Long userId
) {

    return toResponse(
            getEntity(userId)
    );
}

public AdminUserResponse enable(
        Long userId
) {

    User user = getEntity(userId);

    user.setEnabled(true);

    return toResponse(
            userRepository.save(user)
    );
}

public AdminUserResponse disable(
        Long userId
) {

    User user = getEntity(userId);

    user.setEnabled(false);

    return toResponse(
            userRepository.save(user)
    );
}

private User getEntity(
        Long userId
) {

    return userRepository.findById(userId)
            .orElseThrow(() ->
                    new IllegalArgumentException(
                            "User not found: " + userId
                    )
            );
}

private AdminUserResponse toResponse(
        User user
) {

    return new AdminUserResponse(
            user.getId(),
            user.getName(),
            user.getEmail(),
            user.getPhone(),
            user.getRole(),
            user.isEnabled(),
            user.isEmailVerified(),
            user.isPhoneVerified(),
            user.getCreatedAt(),
            user.getUpdatedAt()
    );
}

}