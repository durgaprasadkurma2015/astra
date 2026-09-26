package com.astra.seller.controller;
import com.astra.seller.dto.SellerDashboardResponse; import com.astra.seller.service.SellerSalesService; import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.security.core.Authentication; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/sellers/sales") @PreAuthorize("hasRole('SELLER')")
public class SellerSalesController { private final SellerSalesService service; public SellerSalesController(SellerSalesService service){this.service=service;} @GetMapping("/dashboard") public SellerDashboardResponse dashboard(Authentication a){return service.dashboard(a.getName());} }
