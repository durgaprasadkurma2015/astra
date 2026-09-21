package com.astra.repository;

import com.astra.entity.Address;
import com.astra.entity.User;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByUserOrderByDefaultAddressDescCreatedAtDesc(User user);

    Optional<Address> findByIdAndUser(Long id, User user);

    boolean existsByIdAndUser(Long id, User user);
}