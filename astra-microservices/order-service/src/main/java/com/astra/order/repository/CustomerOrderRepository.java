
package com.astra.order.repository;

import com.astra.order.entity.CustomerOrder;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;

public interface CustomerOrderRepository
        extends JpaRepository<CustomerOrder, Long> {

    Optional<CustomerOrder> findByOrderNumberAndUserEmail(
            String orderNumber, String userEmail);

    Optional<CustomerOrder> findByUserEmailAndCheckoutIdempotencyKey(
            String userEmail, String key);

    List<CustomerOrder> findByUserEmailOrderByCreatedAtDesc(
            String userEmail);

    List<CustomerOrder> findAllByOrderByCreatedAtDesc();

    Optional<CustomerOrder> findByOrderNumber(String orderNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("""
        SELECT o FROM CustomerOrder o
        WHERE o.orderNumber = :orderNumber
    """)
    Optional<CustomerOrder> findByOrderNumberForUpdate(
            @org.springframework.data.repository.query.Param("orderNumber")
            String orderNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @org.springframework.data.jpa.repository.Query("""
        SELECT o FROM CustomerOrder o
        WHERE o.orderNumber = :orderNumber
          AND o.userEmail = :userEmail
    """)
    Optional<CustomerOrder> findByOrderNumberAndUserEmailForUpdate(
            @org.springframework.data.repository.query.Param("orderNumber")
            String orderNumber,
            @org.springframework.data.repository.query.Param("userEmail")
            String userEmail);
}
