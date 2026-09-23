package com.astra.address.controller;

import com.astra.address.dto.AddressRequest;
import com.astra.address.dto.AddressResponse;
import com.astra.address.service.AddressService;
import com.astra.dto.MessageResponse;
import com.astra.entity.User;
import com.astra.repository.UserRepository;

import jakarta.validation.Valid;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/addresses")
public class AddressController {

    private final AddressService addressService;
    private final UserRepository userRepository;

    public AddressController(
            AddressService addressService,
            UserRepository userRepository
    ) {
        this.addressService = addressService;
        this.userRepository = userRepository;
    }

    private User getAuthenticatedUser(
            Authentication authentication
    ) {

        return userRepository
                .findByEmailIgnoreCase(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Authenticated user not found"
                        )
                );
    }

    @GetMapping
    public ResponseEntity<List<AddressResponse>> getAddresses(
            Authentication authentication
    ) {

        User user = getAuthenticatedUser(authentication);

        return ResponseEntity.ok(
                addressService.getAddresses(user.getId())
        );
    }

    @PostMapping
    public ResponseEntity<AddressResponse> createAddress(
            Authentication authentication,
            @Valid @RequestBody AddressRequest request
    ) {

        User user = getAuthenticatedUser(authentication);

        AddressResponse response =
                addressService.create(
                        user.getId(),
                        request
                );

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(response);
    }

    @PutMapping("/{id}")
    public ResponseEntity<AddressResponse> updateAddress(
            Authentication authentication,
            @PathVariable Long id,
            @Valid @RequestBody AddressRequest request
    ) {

        User user = getAuthenticatedUser(authentication);

        return ResponseEntity.ok(
                addressService.update(
                        user.getId(),
                        id,
                        request
                )
        );
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<MessageResponse> deleteAddress(
            Authentication authentication,
            @PathVariable Long id
    ) {

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

    @PutMapping("/{id}/default")
    public ResponseEntity<AddressResponse> makeDefault(
            Authentication authentication,
            @PathVariable Long id
    ) {

        User user = getAuthenticatedUser(authentication);

        return ResponseEntity.ok(
                addressService.makeDefault(
                        user.getId(),
                        id
                )
        );
    }
}
