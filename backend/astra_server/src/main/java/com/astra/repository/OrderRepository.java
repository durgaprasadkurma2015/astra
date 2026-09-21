package com.astra.repository;

import com.astra.entity.Order;
import com.astra.enums.OrderStatus;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<Order, Long> {

    Page<Order> findByUserIdOrderByCreatedAtDesc(
            Long userId,
            Pageable pageable
    );

    Optional<Order> findByIdAndUserId(
            Long id,
            Long userId
    );

    Optional<Order> findByOrderNumber(
            String orderNumber
    );

    boolean existsByOrderNumber(
            String orderNumber
    );

    @Query("""
        SELECT COALESCE(SUM(o.total), 0)
        FROM Order o
    """)
    BigDecimal calculateTotalRevenue();

    @Query("""
        SELECT COUNT(o)
        FROM Order o
        WHERE o.status = com.astra.enums.OrderStatus.PENDING
    """)
    long countPendingOrders();
}
