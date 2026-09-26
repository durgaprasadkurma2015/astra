package com.astra.seller.service;

import com.astra.entity.OrderItem;
import com.astra.entity.Seller;
import com.astra.exception.ApiException;
import com.astra.repository.OrderItemRepository;
import com.astra.repository.SellerRepository;
import com.astra.repository.UserRepository;
import com.astra.seller.dto.SellerOrderResponse;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.*;

@Service @Transactional(readOnly=true)
public class SellerOrderService {
    private final UserRepository users; private final SellerRepository sellers; private final OrderItemRepository items;
    public SellerOrderService(UserRepository users,SellerRepository sellers,OrderItemRepository items){this.users=users;this.sellers=sellers;this.items=items;}
    public Page<SellerOrderResponse> list(String email,int page,int size){ Seller s=getSeller(email); List<OrderItem> all=items.findByProductSellerIdOrderByOrderCreatedAtDesc(s.getId()); Map<Long,SellerOrderResponse> map=new LinkedHashMap<>(); for(OrderItem i:all){var o=i.getOrder(); var old=map.get(o.getId()); BigDecimal subtotal=(old==null?BigDecimal.ZERO:old.sellerSubtotal()).add(i.getLineTotal()); map.put(o.getId(),new SellerOrderResponse(o.getId(),o.getOrderNumber(),o.getStatus(),o.getPaymentStatus(),subtotal,o.getCreatedAt()));} List<SellerOrderResponse> rows=new ArrayList<>(map.values()); int from=Math.min(page*size,rows.size()), to=Math.min(from+size,rows.size()); return new PageImpl<>(rows.subList(from,to),PageRequest.of(page,size),rows.size()); }
    private Seller getSeller(String email){var u=users.findByEmailIgnoreCase(email).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"User not found.")); return sellers.findByUserId(u.getId()).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Seller profile not found."));}
}
