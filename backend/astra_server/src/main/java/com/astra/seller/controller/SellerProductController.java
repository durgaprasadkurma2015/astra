package com.astra.seller.controller;
import com.astra.product.dto.ProductRequest; import com.astra.seller.dto.SellerProductResponse; import com.astra.seller.service.SellerProductService;
import jakarta.validation.Valid; import org.springframework.http.*; import org.springframework.security.access.prepost.PreAuthorize; import org.springframework.security.core.Authentication; import org.springframework.web.bind.annotation.*; import org.springframework.data.domain.Page;
@RestController @RequestMapping("/api/v1/sellers/products") @PreAuthorize("hasRole('SELLER')")
public class SellerProductController { private final SellerProductService service; public SellerProductController(SellerProductService service){this.service=service;}
 @GetMapping public Page<SellerProductResponse> list(Authentication a,@RequestParam(defaultValue="0") int page,@RequestParam(defaultValue="20") int size){return service.list(a.getName(),page,size);}
 @PostMapping public ResponseEntity<SellerProductResponse> create(Authentication a,@Valid @RequestBody ProductRequest r){return ResponseEntity.status(HttpStatus.CREATED).body(service.create(a.getName(),r));}
 @PutMapping("/{id}") public SellerProductResponse update(Authentication a,@PathVariable Long id,@Valid @RequestBody ProductRequest r){return service.update(a.getName(),id,r);}
 @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void delete(Authentication a,@PathVariable Long id){service.deactivate(a.getName(),id);}
}
