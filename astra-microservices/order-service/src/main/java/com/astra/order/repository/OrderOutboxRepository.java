
package com.astra.order.repository;

import com.astra.order.entity.OrderOutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OrderOutboxRepository
        extends JpaRepository<OrderOutboxEvent, Long> {

    List<OrderOutboxEvent>
        findTop50ByEventStatusOrderByCreatedAtAsc(String eventStatus);
}
