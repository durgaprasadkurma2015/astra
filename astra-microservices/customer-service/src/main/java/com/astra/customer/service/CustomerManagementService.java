
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

    public CustomerManagementService(CustomerRepository repository) {
        this.repository = repository;
    }

    // CUSTOMER creates their own profile using JWT userId
    @Transactional
    public CustomerResponse create(
            CustomerRequest request,
            Long authenticatedUserId) {

        if (authenticatedUserId == null) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Authenticated user ID is missing"
            );
        }

        String email = normalizeEmail(request.email());

        if (repository.existsByEmail(email)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Customer email already exists"
            );
        }

        if (repository.existsByAuthUserId(authenticatedUserId)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Customer profile already exists for this user"
            );
        }

        Customer customer = new Customer();

        customer.setAuthUserId(authenticatedUserId);

        applyRequest(customer, request);
        customer.setEmail(email);

        return toResponse(repository.save(customer));
    }

    // ADMIN only
    @Transactional(readOnly = true)
    public List<CustomerResponse> getAll(boolean isAdmin) {

        requireAdmin(isAdmin);

        return repository.findAllByOrderByCreatedAtDesc()
            .stream()
            .map(this::toResponse)
            .toList();
    }

    // ADMIN can access any customer.
    // CUSTOMER can access only their own customer record.
    @Transactional(readOnly = true)
    public CustomerResponse getById(
            Long id,
            Long authenticatedUserId,
            boolean isAdmin) {

        Customer customer = isAdmin
            ? findCustomer(id)
            : findCustomerByOwner(id, authenticatedUserId);

        return toResponse(customer);
    }
    
 // CUSTOMER: Get logged-in customer's own profile
    @Transactional(readOnly = true)
    public CustomerResponse getByAuthUserId(Long authUserId) {

        if (authUserId == null) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Authenticated user ID is missing"
            );
        }

        Customer customer = repository.findByAuthUserId(authUserId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Customer profile not found"
            ));

        return toResponse(customer);
    }
    

    // ADMIN can update any customer.
    // CUSTOMER can update only their own customer record.
    @Transactional
    public CustomerResponse update(
            Long id,
            CustomerRequest request,
            Long authenticatedUserId,
            boolean isAdmin) {

        Customer customer = isAdmin
            ? findCustomer(id)
            : findCustomerByOwner(id, authenticatedUserId);

        String email = normalizeEmail(request.email());

        if (repository.existsByEmailAndIdNot(email, id)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Another customer already uses this email"
            );
        }

        // Do not change authUserId during an update.
        applyRequest(customer, request);
        customer.setEmail(email);

        return toResponse(repository.save(customer));
    }

    // ADMIN only
    @Transactional
    public void delete(Long id, boolean isAdmin) {

        requireAdmin(isAdmin);

        Customer customer = findCustomer(id);
        repository.delete(customer);
    }

    private void requireAdmin(boolean isAdmin) {
        if (!isAdmin) {
            throw new ResponseStatusException(
                HttpStatus.FORBIDDEN,
                "Admin access required"
            );
        }
    }

    private Customer findCustomer(Long id) {
        return repository.findById(id)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Customer not found"
            ));
    }

    private Customer findCustomerByOwner(
            Long id,
            Long authenticatedUserId) {

        if (authenticatedUserId == null) {
            throw new ResponseStatusException(
                HttpStatus.UNAUTHORIZED,
                "Authenticated user ID is missing"
            );
        }

        // Return 404 for both nonexistent and other users' records.
        return repository.findByIdAndAuthUserId(
                id,
                authenticatedUserId
            )
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Customer not found"
            ));
    }

    private void applyRequest(
            Customer customer,
            CustomerRequest request) {

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
