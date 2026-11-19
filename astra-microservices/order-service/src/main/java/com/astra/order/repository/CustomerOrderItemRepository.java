
package com.astra.order.repository;

import com.astra.order.entity.CustomerOrderItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CustomerOrderItemRepository
        extends JpaRepository<CustomerOrderItem, Long> {

    List<CustomerOrderItem> findByOrderId(Long orderId);
}
