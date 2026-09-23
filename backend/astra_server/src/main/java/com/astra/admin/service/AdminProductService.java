package com.astra.admin.service;

import com.astra.admin.dto.AdminProductResponse;
import com.astra.entity.Product;
import com.astra.inventory.service.InventoryService;
import com.astra.repository.ProductRepository;

import jakarta.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class AdminProductService {

    private final ProductRepository productRepository;
    private final InventoryService inventoryService;

    public AdminProductService(
            ProductRepository productRepository,
            InventoryService inventoryService
    ) {
        this.productRepository = productRepository;
        this.inventoryService = inventoryService;
    }

    public Page<AdminProductResponse> getProducts(
            Boolean active,
            Pageable pageable
    ) {

        Page<Product> products;

        if (active == null) {

            products =
                    productRepository
                            .findAllByOrderByCreatedAtDesc(
                                    pageable
                            );

        } else {

            products =
                    productRepository
                            .findByActiveOrderByCreatedAtDesc(
                                    active,
                                    pageable
                            );
        }

        return products.map(this::toResponse);
    }

    public AdminProductResponse getProduct(
            Long productId
    ) {

        Product product =
                productRepository.findById(productId)
                        .orElseThrow(() ->
                                new IllegalArgumentException(
                                        "Product not found: "
                                                + productId
                                )
                        );

        return toResponse(product);
    }

    public AdminProductResponse activate(
            Long productId
    ) {

        Product product = getEntity(productId);

        product.setActive(true);

        return toResponse(
                productRepository.save(product)
        );
    }

    public AdminProductResponse deactivate(
            Long productId
    ) {

        Product product = getEntity(productId);

        product.setActive(false);

        return toResponse(
                productRepository.save(product)
        );
    }

    public AdminProductResponse setFeatured(
            Long productId,
            boolean featured
    ) {

        Product product = getEntity(productId);

        product.setFeatured(featured);

        return toResponse(
                productRepository.save(product)
        );
    }

    public AdminProductResponse restock(
            Long productId,
            int quantity,
            String reason
    ) {

        inventoryService.restock(
                productId,
                quantity,
                reason
        );

        return getProduct(productId);
    }

    public AdminProductResponse adjustInventory(
            Long productId,
            int quantity,
            String reason
    ) {

        com.astra.inventory.dto.InventoryAdjustmentRequest request =
                new com.astra.inventory.dto.InventoryAdjustmentRequest(
                        quantity,
                        reason
                );

        inventoryService.adjust(
                productId,
                request
        );

        return getProduct(productId);
    }

    private Product getEntity(
            Long productId
    ) {

        return productRepository.findById(productId)
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "Product not found: "
                                        + productId
                        )
                );
    }

    private AdminProductResponse toResponse(
            Product product
    ) {

        return new AdminProductResponse(
                product.getId(),
                product.getName(),
                product.getSlug(),
                product.getPrice(),
                product.getDiscountPrice(),
                product.getStockQuantity(),
                product.isActive(),
                product.getRating()
        );
    }
}
