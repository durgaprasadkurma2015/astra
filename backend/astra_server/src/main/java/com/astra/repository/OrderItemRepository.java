package com.astra.repository;

import com.astra.entity.OrderItem;

import org.springframework.data.jpa.repository.JpaRepository;

public interface OrderItemRepository
        extends JpaRepository<OrderItem, Long> {
}