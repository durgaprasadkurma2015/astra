package com.astra.backend.order;

import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CustomerOrderRepository
        extends JpaRepository<CustomerOrder, Long> {

    Optional<CustomerOrder> findByOrderNumberAndUserEmail(
            String orderNumber,
            String userEmail
    );

    List<CustomerOrder> findByUserEmailOrderByCreatedAtDesc(
            String userEmail
    );

    Page<CustomerOrder> findByUserEmailOrderByCreatedAtDesc(
            String userEmail,
            Pageable pageable
    );

    Optional<CustomerOrder> findByUserEmailAndCheckoutIdempotencyKey(
            String userEmail,
            String checkoutIdempotencyKey
    );

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT o
            FROM CustomerOrder o
            WHERE o.orderNumber = :orderNumber
              AND o.userEmail = :userEmail
            """)
    Optional<CustomerOrder> findByOrderNumberAndUserEmailForUpdate(
            @Param("orderNumber") String orderNumber,
            @Param("userEmail") String userEmail
    );

    List<CustomerOrder> findAllByOrderByCreatedAtDesc();

    Page<CustomerOrder> findAllByOrderByCreatedAtDesc(Pageable pageable);

    @Query("""
            SELECT o FROM CustomerOrder o
            WHERE (:status IS NULL OR UPPER(o.orderStatus) = UPPER(:status))
              AND (:query IS NULL OR
                   LOWER(o.orderNumber) LIKE LOWER(CONCAT('%', :query, '%')) OR
                   LOWER(o.customerName) LIKE LOWER(CONCAT('%', :query, '%')) OR
                   LOWER(o.userEmail) LIKE LOWER(CONCAT('%', :query, '%')))
            """)
    Page<CustomerOrder> searchAdminOrders(
            @Param("query") String query,
            @Param("status") String status,
            Pageable pageable
    );

    Optional<CustomerOrder> findByOrderNumber(String orderNumber);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT o FROM CustomerOrder o WHERE o.razorpayOrderId = :providerOrderId")
    Optional<CustomerOrder> findByRazorpayOrderIdForUpdate(
            @Param("providerOrderId") String providerOrderId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT o
            FROM CustomerOrder o
            WHERE o.orderNumber = :orderNumber
            """)
    Optional<CustomerOrder> findByOrderNumberForUpdate(
            @Param("orderNumber") String orderNumber
    );
}
