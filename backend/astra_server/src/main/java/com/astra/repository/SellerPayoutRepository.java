package com.astra.repository;

import com.astra.entity.SellerPayout;
import com.astra.enums.PayoutStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SellerPayoutRepository extends JpaRepository<SellerPayout, Long> {
    Page<SellerPayout> findBySellerIdOrderByCreatedAtDesc(Long sellerId, Pageable pageable);
    long countBySellerIdAndStatus(Long sellerId, PayoutStatus status);
}
