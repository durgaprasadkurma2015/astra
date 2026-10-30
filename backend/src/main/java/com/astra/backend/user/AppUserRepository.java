package com.astra.backend.user;

import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.jpa.repository.JpaRepository;

public interface AppUserRepository
        extends JpaRepository<AppUser, Long> {

    Optional<AppUser> findByEmail(String email);

    boolean existsByEmail(String email);

    @Query("""
        SELECT u FROM AppUser u
        WHERE u.role = com.astra.backend.user.Role.CUSTOMER
          AND (:keyword IS NULL OR
               LOWER(u.email) LIKE LOWER(CONCAT('%', :keyword, '%')) OR
               LOWER(u.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')))
        """)
    Page<AppUser> searchCustomers(@Param("keyword") String keyword,
            Pageable pageable);
}
