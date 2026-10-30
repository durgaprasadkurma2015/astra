package com.astra.backend.user;

import com.astra.backend.user.dto.CustomerAddressRequest;
import com.astra.backend.user.dto.CustomerAddressResponse;
import java.util.List;
import java.util.Locale;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class CustomerAddressService {

    private final CustomerAddressRepository addresses;

    public CustomerAddressService(CustomerAddressRepository addresses) {
        this.addresses = addresses;
    }

    @Transactional(readOnly = true)
    public List<CustomerAddressResponse> list() {
        String email = currentEmail();
        return addresses.findByUserEmailOrderByDefaultAddressDescIdDesc(email)
                .stream().map(CustomerAddressResponse::from).toList();
    }

    @Transactional
    public CustomerAddressResponse create(CustomerAddressRequest request) {
        String email = currentEmail();
        CustomerAddress address = new CustomerAddress();
        address.setUserEmail(email);
        apply(address, request);
        address.setDefaultAddress(request.defaultAddress()
                || !addresses.existsByUserEmail(email));
        if (address.isDefaultAddress()) {
            clearDefault(email, null);
        }
        return CustomerAddressResponse.from(addresses.save(address));
    }

    @Transactional
    public CustomerAddressResponse update(Long id, CustomerAddressRequest request) {
        String email = currentEmail();
        CustomerAddress address = findOwned(id, email);
        boolean wasDefault = address.isDefaultAddress();
        apply(address, request);
        address.setDefaultAddress(request.defaultAddress());
        if (request.defaultAddress()) {
            clearDefault(email, id);
        } else if (wasDefault) {
            setFirstAddressDefault(email, id);
        }
        return CustomerAddressResponse.from(addresses.save(address));
    }

    @Transactional
    public CustomerAddressResponse makeDefault(Long id) {
        String email = currentEmail();
        CustomerAddress address = findOwned(id, email);
        clearDefault(email, id);
        address.setDefaultAddress(true);
        return CustomerAddressResponse.from(addresses.save(address));
    }

    @Transactional
    public void delete(Long id) {
        String email = currentEmail();
        CustomerAddress address = findOwned(id, email);
        boolean wasDefault = address.isDefaultAddress();
        addresses.delete(address);
        if (wasDefault) {
            setFirstAddressDefault(email, null);
        }
    }

    private CustomerAddress findOwned(Long id, String email) {
        return addresses.findByIdAndUserEmail(id, email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Address not found"));
    }

    private void clearDefault(String email, Long exceptId) {
        addresses.findByUserEmailOrderByDefaultAddressDescIdDesc(email)
                .stream()
                .filter(CustomerAddress::isDefaultAddress)
                .filter(address -> exceptId == null || !exceptId.equals(address.getId()))
                .forEach(address -> address.setDefaultAddress(false));
    }

    private void setFirstAddressDefault(String email, Long excludedId) {
        addresses.findByUserEmailOrderByDefaultAddressDescIdDesc(email)
                .stream()
                .filter(address -> excludedId == null || !excludedId.equals(address.getId()))
                .findFirst()
                .ifPresent(address -> address.setDefaultAddress(true));
    }

    private void apply(CustomerAddress target, CustomerAddressRequest request) {
        target.setLabel(clean(request.label()));
        target.setFullName(clean(request.fullName()));
        target.setPhoneNumber(clean(request.phoneNumber()));
        target.setAddressLine1(clean(request.addressLine1()));
        target.setAddressLine2(clean(request.addressLine2()));
        target.setLandmark(clean(request.landmark()));
        target.setCity(clean(request.city()));
        target.setDistrict(clean(request.district()));
        target.setState(clean(request.state()));
        target.setPostalCode(clean(request.postalCode()));
        target.setCountry(clean(request.country()));
    }

    private String clean(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private String currentEmail() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()
                || authentication.getName() == null
                || "anonymousUser".equalsIgnoreCase(authentication.getName())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Please log in");
        }
        return authentication.getName().trim().toLowerCase(Locale.ROOT);
    }
}
