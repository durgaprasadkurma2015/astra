package com.astra.seller.service;

import com.astra.repository.OrderItemRepository;
import com.astra.repository.ProductRepository;
import com.astra.repository.SellerRepository;
import com.astra.repository.UserRepository;
import com.astra.seller.dto.SellerDashboardResponse;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;

@Service @Transactional(readOnly=true)
public class SellerSalesService {
    private final UserRepository users; private final SellerRepository sellers; private final ProductRepository products; private final OrderItemRepository items;
    public SellerSalesService(UserRepository users,SellerRepository sellers,ProductRepository products,OrderItemRepository items){this.users=users;this.sellers=sellers;this.products=products;this.items=items;}
    public SellerDashboardResponse dashboard(String email){var u=users.findByEmailIgnoreCase(email).orElseThrow(); var s=sellers.findByUserId(u.getId()).orElseThrow(); return new SellerDashboardResponse(products.countBySellerId(s.getId()),products.countBySellerIdAndActiveTrue(s.getId()),products.countBySellerIdAndStockQuantityLessThanEqual(s.getId(),10),items.countDistinctOrdersBySellerId(s.getId()),items.countPendingOrdersBySellerId(s.getId()),items.countDeliveredOrdersBySellerId(s.getId()),nz(items.calculatePaidSalesBySellerId(s.getId())),nz(items.calculatePendingPayoutBySellerId(s.getId()))); }
    private BigDecimal nz(BigDecimal v){return v==null?BigDecimal.ZERO:v;}
}
