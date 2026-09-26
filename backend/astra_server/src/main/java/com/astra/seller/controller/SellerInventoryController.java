package com.astra.seller.controller;
import com.astra.inventory.dto.*; import com.astra.seller.service.SellerInventoryService; import jakarta.validation.Valid; import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.security.core.Authentication; import org.springframework.web.bind.annotation.*;
@RestController @RequestMapping("/api/v1/sellers/inventory") @PreAuthorize("hasRole('SELLER')")
public class SellerInventoryController { private final SellerInventoryService service; public SellerInventoryController(SellerInventoryService service){this.service=service;}
 @GetMapping("/{productId}") public InventoryResponse get(Authentication a,@PathVariable Long productId){return service.get(a.getName(),productId);}
 @PostMapping("/{productId}/restock") public InventoryResponse restock(Authentication a,@PathVariable Long productId,@Valid @RequestBody InventoryAdjustmentRequest r){return service.restock(a.getName(),productId,r);}
}
