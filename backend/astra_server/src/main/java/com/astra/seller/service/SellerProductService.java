package com.astra.seller.service;

import com.astra.entity.Category;
import com.astra.entity.Product;
import com.astra.entity.Seller;
import com.astra.entity.User;
import com.astra.exception.ApiException;
import com.astra.product.dto.ProductRequest;
import com.astra.repository.CategoryRepository;
import com.astra.repository.ProductRepository;
import com.astra.repository.SellerRepository;
import com.astra.repository.UserRepository;
import com.astra.seller.dto.SellerProductResponse;
import org.springframework.data.domain.*;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SellerProductService {
    private final UserRepository users; private final SellerRepository sellers;
    private final ProductRepository products; private final CategoryRepository categories;
    public SellerProductService(UserRepository users, SellerRepository sellers, ProductRepository products, CategoryRepository categories) {
        this.users=users; this.sellers=sellers; this.products=products; this.categories=categories;
    }
    public Page<SellerProductResponse> list(String email,int page,int size){
        Seller s=getSeller(email); Pageable p=PageRequest.of(Math.max(page,0),Math.min(Math.max(size,1),100),Sort.by("createdAt").descending());
        return products.findBySellerIdOrderByCreatedAtDesc(s.getId(),p).map(this::toResponse);
    }
    public SellerProductResponse create(String email, ProductRequest r){
        Seller s=getActiveSeller(email); Category c=categories.findById(r.categoryId()).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Category not found."));
        String slug=slug(r.name());
        if(products.existsBySlug(slug)) throw new ApiException(HttpStatus.CONFLICT,"Product slug already exists.");
        if(r.sku()!=null && !r.sku().isBlank() && products.existsBySku(r.sku().trim())) throw new ApiException(HttpStatus.CONFLICT,"SKU already exists.");
        Product p=Product.builder().name(r.name().trim()).slug(slug).sku(blank(r.sku())).shortDescription(blank(r.shortDescription())).description(blank(r.description())).price(r.price()).discountPrice(r.discountPrice()).stockQuantity(r.stockQuantity()).active(r.active()).featured(false).rating(0d).reviewCount(0L).salesCount(0L).category(c).thumbnailUrl(blank(r.thumbnailUrl())).seller(s).build();
        return toResponse(products.save(p));
    }
    public SellerProductResponse update(String email,Long id,ProductRequest r){
        Seller s=getActiveSeller(email); Product p=owned(s,id); Category c=categories.findById(r.categoryId()).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Category not found."));
        String slug=slug(r.name()); if(!slug.equals(p.getSlug()) && products.existsBySlug(slug)) throw new ApiException(HttpStatus.CONFLICT,"Product slug already exists.");
        if(r.sku()!=null && !r.sku().isBlank() && !r.sku().equals(p.getSku()) && products.existsBySku(r.sku().trim())) throw new ApiException(HttpStatus.CONFLICT,"SKU already exists.");
        p.setName(r.name().trim()); p.setSlug(slug); p.setSku(blank(r.sku())); p.setShortDescription(blank(r.shortDescription())); p.setDescription(blank(r.description())); p.setPrice(r.price()); p.setDiscountPrice(r.discountPrice()); p.setStockQuantity(r.stockQuantity()); p.setActive(r.active()); p.setCategory(c); p.setThumbnailUrl(blank(r.thumbnailUrl()));
        return toResponse(products.save(p));
    }
    public void deactivate(String email,Long id){ Seller s=getActiveSeller(email); Product p=owned(s,id); p.setActive(false); products.save(p); }
    private Seller getSeller(String email){ User u=users.findByEmailIgnoreCase(email).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"User not found.")); return sellers.findByUserId(u.getId()).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Seller profile not found.")); }
    private Seller getActiveSeller(String email){ Seller s=getSeller(email); if(s.getStatus()!=com.astra.enums.SellerStatus.ACTIVE) throw new ApiException(HttpStatus.FORBIDDEN,"Seller account is not active."); return s; }
    private Product owned(Seller s,Long id){ Product p=products.findById(id).orElseThrow(()->new ApiException(HttpStatus.NOT_FOUND,"Product not found.")); if(p.getSeller()==null || !p.getSeller().getId().equals(s.getId())) throw new ApiException(HttpStatus.FORBIDDEN,"Product does not belong to this seller."); return p; }
    private SellerProductResponse toResponse(Product p){ return new SellerProductResponse(p.getId(),p.getName(),p.getSlug(),p.getSku(),p.getPrice(),p.getDiscountPrice(),p.getStockQuantity(),p.isActive(),p.isFeatured(),p.getCategory().getId(),p.getCategory().getName()); }
    private String slug(String v){ return v.trim().toLowerCase().replaceAll("[^a-z0-9]+","-").replaceAll("(^-|-$)",""); }
    private String blank(String v){ return v==null||v.isBlank()?null:v.trim(); }
}
