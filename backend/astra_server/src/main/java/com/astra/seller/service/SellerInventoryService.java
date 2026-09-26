package com.astra.seller.service;

import com.astra.entity.Seller;
import com.astra.exception.ApiException;
import com.astra.inventory.dto.InventoryAdjustmentRequest;
import com.astra.inventory.dto.InventoryResponse;
import com.astra.inventory.service.InventoryService;
import com.astra.repository.ProductRepository;
import com.astra.repository.SellerRepository;
import com.astra.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service @Transactional
public class SellerInventoryService {
    private final SellerProductService products; private final InventoryService inventory; private final UserRepository users; private final SellerRepository sellers; private final ProductRepository productRepository;
    public SellerInventoryService(SellerProductService products, InventoryService inventory, UserRepository users, SellerRepository sellers, ProductRepository productRepository){this.products=products;this.inventory=inventory;this.users=users;this.sellers=sellers;this.productRepository=productRepository;}
    public InventoryResponse get(String email,Long productId){ owns(email,productId); return inventory.getInventory(productId); }
    public InventoryResponse restock(String email,Long productId,InventoryAdjustmentRequest r){ owns(email,productId); if(r.quantity()==null||r.quantity()<=0) throw new ApiException(HttpStatus.BAD_REQUEST,"Quantity must be greater than zero."); return inventory.restock(productId,r.quantity(),r.reason()); }
    private void owns(String email,Long id){ Seller s=sellers.findByUserId(users.findByEmailIgnoreCase(email).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"User not found.")).getId()).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Seller profile not found.")); var p=productRepository.findById(id).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Product not found.")); if(p.getSeller()==null||!p.getSeller().getId().equals(s.getId())) throw new ApiException(HttpStatus.FORBIDDEN,"Product does not belong to this seller."); }
}
