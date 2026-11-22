
package com.astra.payment.repository;

import com.astra.payment.entity.PaymentEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PaymentEventRepository
        extends JpaRepository<PaymentEvent, Long> {

    List<PaymentEvent> findTop50ByEventStatusOrderByCreatedAtAsc(
        String eventStatus
    );
}
