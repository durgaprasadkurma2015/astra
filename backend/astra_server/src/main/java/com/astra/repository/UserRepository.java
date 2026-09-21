package com.astra.repository;

import com.astra.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);
    Optional<User> findByPhone(String phone);
    Optional<User> findByGoogleSubject(String googleSubject);
    boolean existsByEmailIgnoreCase(String email);
    boolean existsByPhone(String phone);
}
