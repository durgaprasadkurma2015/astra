package com.astra.backend.user.dto;

import com.astra.backend.user.CustomerAddress;
import java.time.LocalDateTime;

public record CustomerAddressResponse(
        Long id,
        String label,
        String fullName,
        String phoneNumber,
        String addressLine1,
        String addressLine2,
        String landmark,
        String city,
        String district,
        String state,
        String postalCode,
        String country,
        boolean defaultAddress,
        LocalDateTime createdAt,
        LocalDateTime updatedAt) {

    public static CustomerAddressResponse from(CustomerAddress address) {
        return new CustomerAddressResponse(
                address.getId(), address.getLabel(), address.getFullName(),
                address.getPhoneNumber(), address.getAddressLine1(),
                address.getAddressLine2(), address.getLandmark(),
                address.getCity(), address.getDistrict(), address.getState(),
                address.getPostalCode(), address.getCountry(),
                address.isDefaultAddress(), address.getCreatedAt(),
                address.getUpdatedAt());
    }
}
