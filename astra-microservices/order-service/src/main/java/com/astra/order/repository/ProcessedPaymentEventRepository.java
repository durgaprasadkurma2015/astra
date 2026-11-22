package com.astra.order.repository;

import com.astra.order.entity.ProcessedPaymentEvent;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProcessedPaymentEventRepository
        extends JpaRepository<ProcessedPaymentEvent, Long> {

    boolean existsByEventId(String eventId);
}