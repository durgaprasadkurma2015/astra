package com.astra.seller.service;

import com.astra.entity.SellerPayout;
import com.astra.enums.PayoutStatus;
import com.astra.exception.ApiException;
import com.astra.repository.OrderItemRepository;
import com.astra.repository.SellerPayoutRepository;
import com.astra.repository.SellerRepository;
import com.astra.repository.UserRepository;
import com.astra.seller.dto.SellerPayoutResponse;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime; import java.util.UUID;

@Service @Transactional
public class SellerPayoutService {
 private final UserRepository users; private final SellerRepository sellers; private final SellerPayoutRepository payouts; private final OrderItemRepository items;
 public SellerPayoutService(UserRepository users,SellerRepository sellers,SellerPayoutRepository payouts,OrderItemRepository items){this.users=users;this.sellers=sellers;this.payouts=payouts;this.items=items;}
 @Transactional(readOnly=true) public Page<SellerPayoutResponse> list(String email,int page,int size){var s=getSeller(email);return payouts.findBySellerIdOrderByCreatedAtDesc(s.getId(),PageRequest.of(Math.max(0,page),Math.min(Math.max(size,1),100))).map(this::to);}
 public SellerPayoutResponse request(String email){var s=getSeller(email);var amount=items.calculatePaidSalesBySellerId(s.getId());if(amount==null||amount.signum()<=0)throw new ApiException(HttpStatus.BAD_REQUEST,"No paid sales are available for payout.");var p=SellerPayout.builder().seller(s).amount(amount).payoutReference("ASTRA-PAY-"+UUID.randomUUID().toString().replace("-","").substring(0,16).toUpperCase()).status(PayoutStatus.PENDING).build();return to(payouts.save(p));}
 private com.astra.entity.Seller getSeller(String email){var u=users.findByEmailIgnoreCase(email).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"User not found."));return sellers.findByUserId(u.getId()).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Seller profile not found."));}
 private SellerPayoutResponse to(SellerPayout p){return new SellerPayoutResponse(p.getId(),p.getAmount(),p.getStatus(),p.getPayoutReference(),p.getFailureReason(),p.getCreatedAt(),p.getProcessedAt());}
}
