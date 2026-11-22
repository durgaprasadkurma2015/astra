
package com.astra.customer.repository;

import com.astra.customer.entity.Customer;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
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

    // Find customer belonging to authenticated user
    Optional<Customer> findByAuthUserId(Long authUserId);

    // Find a specific customer only if it belongs to authenticated user
    Optional<Customer> findByIdAndAuthUserId(
        Long id,
        Long authUserId
    );

    // Admin can retrieve all customers
    List<Customer> findAllByOrderByCreatedAtDesc();
}
