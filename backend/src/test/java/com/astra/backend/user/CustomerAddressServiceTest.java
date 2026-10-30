package com.astra.backend.user;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import com.astra.backend.user.dto.CustomerAddressRequest;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.server.ResponseStatusException;

class CustomerAddressServiceTest {

    private final CustomerAddressRepository repository =
            org.mockito.Mockito.mock(CustomerAddressRepository.class);
    private final CustomerAddressService service =
            new CustomerAddressService(repository);

    @AfterEach
    void clearAuthentication() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void firstAddressBecomesDefaultAndIsOwnedByAuthenticatedCustomer() {
        authenticate("shopper@example.test");
        when(repository.existsByUserEmail("shopper@example.test"))
                .thenReturn(false);
        when(repository.findByUserEmailOrderByDefaultAddressDescIdDesc(
                "shopper@example.test")).thenReturn(List.of());
        when(repository.save(any(CustomerAddress.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.create(addressRequest(false));

        assertTrue(response.defaultAddress());
        assertEquals("Home", response.label());
    }

    @Test
    void customerCannotDeleteAnotherCustomersAddress() {
        authenticate("shopper@example.test");
        when(repository.findByIdAndUserEmail(7L, "shopper@example.test"))
                .thenReturn(Optional.empty());

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> service.delete(7L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
    }

    private void authenticate(String email) {
        var authentication = UsernamePasswordAuthenticationToken.authenticated(
                email, "unused", List.of());
        SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    private CustomerAddressRequest addressRequest(boolean defaultAddress) {
        return new CustomerAddressRequest("Home", "Sample Customer",
                "+91 9999999999", "10 Example Road", null, null,
                "Pune", null, "Maharashtra", "411001", "India",
                defaultAddress);
    }
}
