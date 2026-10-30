package com.astra.backend.user;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CustomerAddressRepository extends JpaRepository<CustomerAddress, Long> {
    List<CustomerAddress> findByUserEmailOrderByDefaultAddressDescIdDesc(String userEmail);
    Optional<CustomerAddress> findByIdAndUserEmail(Long id, String userEmail);
    boolean existsByUserEmail(String userEmail);
}
