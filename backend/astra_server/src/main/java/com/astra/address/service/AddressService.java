package com.astra.address.service;

import com.astra.address.dto.AddressRequest;
import com.astra.address.dto.AddressResponse;
import com.astra.entity.Address;
import com.astra.entity.User;
import com.astra.exception.ApiException;
import com.astra.repository.AddressRepository;
import com.astra.user.service.UserService;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AddressService {

    private final AddressRepository addressRepository;
    private final UserService userService;

    public AddressService(
            AddressRepository addressRepository,
            UserService userService) {

        this.addressRepository = addressRepository;
        this.userService = userService;
    }

    public List<AddressResponse> getAddresses(Long userId) {

        User user = userService.getUser(userId);

        return addressRepository
                .findByUserOrderByDefaultAddressDescCreatedAtDesc(user)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public AddressResponse create(
            Long userId,
            AddressRequest request) {

        User user = userService.getUser(userId);

        if (request.defaultAddress()) {
            clearDefaultAddress(user);
        }

        Address address = Address.builder()
                .user(user)
                .fullName(request.fullName())
                .phone(request.phone())
                .addressLine1(request.addressLine1())
                .addressLine2(request.addressLine2())
                .city(request.city())
                .state(request.state())
                .postalCode(request.postalCode())
                .country(request.country())
                .defaultAddress(request.defaultAddress())
                .build();

        return toResponse(addressRepository.save(address));
    }

    public AddressResponse update(
            Long userId,
            Long addressId,
            AddressRequest request) {

        User user = userService.getUser(userId);

        Address address = addressRepository
                .findByIdAndUser(addressId, user)
                .orElseThrow(() ->
                        new ApiException(
                                HttpStatus.NOT_FOUND,
                                "Address not found."
                        )
                );

        if (request.defaultAddress()) {
            clearDefaultAddress(user);
        }

        address.setFullName(request.fullName());
        address.setPhone(request.phone());
        address.setAddressLine1(request.addressLine1());
        address.setAddressLine2(request.addressLine2());
        address.setCity(request.city());
        address.setState(request.state());
        address.setPostalCode(request.postalCode());
        address.setCountry(request.country());
        address.setDefaultAddress(request.defaultAddress());

        return toResponse(addressRepository.save(address));
    }

    public void delete(
            Long userId,
            Long addressId) {

        User user = userService.getUser(userId);

        Address address = addressRepository
                .findByIdAndUser(addressId, user)
                .orElseThrow(() ->
                        new ApiException(
                                HttpStatus.NOT_FOUND,
                                "Address not found."
                        )
                );

        addressRepository.delete(address);
    }

    public AddressResponse makeDefault(
            Long userId,
            Long addressId) {

        User user = userService.getUser(userId);

        Address address = addressRepository
                .findByIdAndUser(addressId, user)
                .orElseThrow(() ->
                        new ApiException(
                                HttpStatus.NOT_FOUND,
                                "Address not found."
                        )
                );

        clearDefaultAddress(user);

        address.setDefaultAddress(true);

        return toResponse(addressRepository.save(address));
    }

    private void clearDefaultAddress(User user) {

        List<Address> addresses =
                addressRepository
                        .findByUserOrderByDefaultAddressDescCreatedAtDesc(user);

        addresses.forEach(address ->
                address.setDefaultAddress(false)
        );

        addressRepository.saveAll(addresses);
    }

    private AddressResponse toResponse(Address address) {

        return new AddressResponse(
                address.getId(),
                address.getFullName(),
                address.getPhone(),
                address.getAddressLine1(),
                address.getAddressLine2(),
                address.getCity(),
                address.getState(),
                address.getPostalCode(),
                address.getCountry(),
                address.isDefaultAddress()
        );
    }
}