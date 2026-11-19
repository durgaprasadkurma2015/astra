package com.astra.customer.repository;

import com.astra.customer.entity.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CustomerRepository
        extends JpaRepository<Customer, Long> {

    boolean existsByEmail(String email);

    boolean existsByEmailAndIdNot(String email, Long id);

    boolean existsByAuthUserId(Long authUserId);

    boolean existsByAuthUserIdAndIdNot(
        Long authUserId,
        Long id
    );

    Optional<Customer> findByEmail(String email);
}