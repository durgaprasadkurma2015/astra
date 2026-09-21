package com.astra.user.controller;

import com.astra.address.dto.AddressRequest;
import com.astra.address.dto.AddressResponse;
import com.astra.address.service.AddressService;
import com.astra.dto.*;
import com.astra.entity.User;
import com.astra.repository.UserRepository;
import com.astra.user.dto.ChangePasswordRequest;
import com.astra.user.service.UserService;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    private final UserService userService;
    private final AddressService addressService;
    private final UserRepository userRepository;

    public UserController(
            UserService userService,
            AddressService addressService,
            UserRepository userRepository) {

        this.userService = userService;
        this.addressService = addressService;
        this.userRepository = userRepository;
    }

    private User getAuthenticatedUser(Authentication authentication) {

        return userRepository.findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("Authenticated user not found"));
    }

    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getProfile(
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);

        return ResponseEntity.ok(
                userService.getProfile(user.getId())
        );
    }

    @PutMapping("/me")
    public ResponseEntity<UserProfileResponse> updateProfile(
            Authentication authentication,
            @Valid @RequestBody UpdateProfileRequest request) {

        User user = getAuthenticatedUser(authentication);

        return ResponseEntity.ok(
                userService.updateProfile(
                        user.getId(),
                        request
                )
        );
    }

    @PutMapping("/me/password")
    public ResponseEntity<MessageResponse> changePassword(
            Authentication authentication,
            @Valid @RequestBody ChangePasswordRequest request) {

        User user = getAuthenticatedUser(authentication);

        userService.changePassword(
                user.getId(),
                request
        );

        return ResponseEntity.ok(
                new MessageResponse(
                        "Password changed successfully."
                )
        );
    }

    @GetMapping("/me/addresses")
    public ResponseEntity<List<AddressResponse>> getAddresses(
            Authentication authentication) {

        User user = getAuthenticatedUser(authentication);

        return ResponseEntity.ok(
                addressService.getAddresses(user.getId())
        );
    }

    @PostMapping("/me/addresses")
    public ResponseEntity<AddressResponse> createAddress(
            Authentication authentication,
            @Valid @RequestBody AddressRequest request) {

        User user = getAuthenticatedUser(authentication);

        return ResponseEntity.ok(
                addressService.create(
                        user.getId(),
                        request
                )
        );
    }

    @PutMapping("/me/addresses/{id}")
    public ResponseEntity<AddressResponse> updateAddress(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody AddressRequest request) {

        User user = getAuthenticatedUser(authentication);

        return ResponseEntity.ok(
                addressService.update(
                        user.getId(),
                        id,
                        request
                )
        );
    }

    @DeleteMapping("/me/addresses/{id}")
    public ResponseEntity<MessageResponse> deleteAddress(
            Authentication authentication,
            @PathVariable Long id) {

        User user = getAuthenticatedUser(authentication);

        addressService.delete(
                user.getId(),
                id
        );

        return ResponseEntity.ok(
                new MessageResponse(
                        "Address deleted successfully."
                )
        );
    }

    @PutMapping("/me/addresses/{id}/default")
    public ResponseEntity<AddressResponse> makeDefault(
            Authentication authentication,
            @PathVariable Long id) {

        User user = getAuthenticatedUser(authentication);

        return ResponseEntity.ok(
                addressService.makeDefault(
                        user.getId(),
                        id
                )
        );
    }
}
