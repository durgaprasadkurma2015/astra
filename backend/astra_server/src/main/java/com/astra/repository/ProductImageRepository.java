package com.astra.repository;

import com.astra.entity.ProductImage;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProductImageRepository
        extends JpaRepository<ProductImage, Long> {

    List<ProductImage>
    findByProductIdOrderByDisplayOrderAsc(Long productId);

    Optional<ProductImage>
    findByIdAndProductId(Long imageId, Long productId);

    Optional<ProductImage>
    findByProductIdAndPrimaryImageTrue(Long productId);

    long countByProductId(Long productId);

    void deleteByProductId(Long productId);
}