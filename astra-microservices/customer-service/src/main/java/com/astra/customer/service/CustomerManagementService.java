package com.astra.customer.service;

import com.astra.customer.dto.CustomerRequest;
import com.astra.customer.dto.CustomerResponse;
import com.astra.customer.entity.Customer;
import com.astra.customer.repository.CustomerRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Locale;

@Service
public class CustomerManagementService {

    private final CustomerRepository repository;

    public CustomerManagementService(
            CustomerRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public CustomerResponse create(CustomerRequest request) {

        String email = normalizeEmail(request.email());

        if (repository.existsByEmail(email)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Customer email already exists"
            );
        }

        if (request.authUserId() != null
                && repository.existsByAuthUserId(request.authUserId())) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Customer is already linked to an auth user"
            );
        }

        Customer customer = new Customer();
        applyRequest(customer, request);
        customer.setEmail(email);

        return toResponse(repository.save(customer));
    }

    @Transactional(readOnly = true)
    public List<CustomerResponse> getAll() {
        return repository.findAll()
            .stream()
            .map(this::toResponse)
            .toList();
    }

    @Transactional(readOnly = true)
    public CustomerResponse getById(Long id) {
        return toResponse(findCustomer(id));
    }

    @Transactional
    public CustomerResponse update(
            Long id,
            CustomerRequest request) {

        Customer customer = findCustomer(id);
        String email = normalizeEmail(request.email());

        if (repository.existsByEmailAndIdNot(email, id)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Another customer already uses this email"
            );
        }

        if (request.authUserId() != null
                && repository.existsByAuthUserIdAndIdNot(
                    request.authUserId(), id)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Auth user is already linked to another customer"
            );
        }

        applyRequest(customer, request);
        customer.setEmail(email);

        return toResponse(repository.save(customer));
    }

    @Transactional
    public void delete(Long id) {
        Customer customer = findCustomer(id);
        repository.delete(customer);
    }

    private Customer findCustomer(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Customer not found: " + id
            ));
    }

    private void applyRequest(
            Customer customer,
            CustomerRequest request) {

        customer.setAuthUserId(request.authUserId());
        customer.setFullName(request.fullName().trim());
        customer.setDateOfBirth(request.dateOfBirth());
        customer.setGender(blankToNull(request.gender()));
        customer.setPhoneNumber(blankToNull(request.phoneNumber()));
        customer.setAlternatePhoneNumber(
            blankToNull(request.alternatePhoneNumber())
        );
        customer.setAddressLine1(blankToNull(request.addressLine1()));
        customer.setAddressLine2(blankToNull(request.addressLine2()));
        customer.setLandmark(blankToNull(request.landmark()));
        customer.setCity(blankToNull(request.city()));
        customer.setDistrict(blankToNull(request.district()));
        customer.setState(blankToNull(request.state()));
        customer.setCountry(blankToNull(request.country()));
        customer.setPostalCode(blankToNull(request.postalCode()));
        customer.setPreferredLanguage(
            blankToNull(request.preferredLanguage())
        );
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank()
            ? null
            : value.trim();
    }

    private String normalizeEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    private CustomerResponse toResponse(Customer c) {
        return new CustomerResponse(
            c.getId(),
            c.getAuthUserId(),
            c.getFullName(),
            c.getEmail(),
            c.getDateOfBirth(),
            c.getGender(),
            c.getPhoneNumber(),
            c.getAlternatePhoneNumber(),
            c.getAddressLine1(),
            c.getAddressLine2(),
            c.getLandmark(),
            c.getCity(),
            c.getDistrict(),
            c.getState(),
            c.getCountry(),
            c.getPostalCode(),
            c.getPreferredLanguage(),
            c.getCreatedAt(),
            c.getUpdatedAt()
        );
    }
}