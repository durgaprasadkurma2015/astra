package com.astra.repository;

import com.astra.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;

public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {
    List<OrderItem> findByProductSellerIdOrderByOrderCreatedAtDesc(Long sellerId);

    @Query("""
        select count(distinct oi.order.id) from OrderItem oi
        where oi.product.seller.id = :sellerId
    """)
    long countDistinctOrdersBySellerId(@Param("sellerId") Long sellerId);

    @Query("""
        select count(distinct oi.order.id) from OrderItem oi
        where oi.product.seller.id = :sellerId
          and oi.order.status = com.astra.enums.OrderStatus.PENDING
    """)
    long countPendingOrdersBySellerId(@Param("sellerId") Long sellerId);

    @Query("""
        select count(distinct oi.order.id) from OrderItem oi
        where oi.product.seller.id = :sellerId
          and oi.order.status = com.astra.enums.OrderStatus.DELIVERED
    """)
    long countDeliveredOrdersBySellerId(@Param("sellerId") Long sellerId);

    @Query("""
        select coalesce(sum(oi.lineTotal),0) from OrderItem oi
        where oi.product.seller.id = :sellerId
          and oi.order.paymentStatus = com.astra.enums.PaymentStatus.PAID
    """)
    BigDecimal calculatePaidSalesBySellerId(@Param("sellerId") Long sellerId);

    @Query("""
        select coalesce(sum(oi.lineTotal),0) from OrderItem oi
        where oi.product.seller.id = :sellerId
          and oi.order.paymentStatus <> com.astra.enums.PaymentStatus.PAID
          and oi.order.status <> com.astra.enums.OrderStatus.CANCELLED
    """)
    BigDecimal calculatePendingPayoutBySellerId(@Param("sellerId") Long sellerId);
}
