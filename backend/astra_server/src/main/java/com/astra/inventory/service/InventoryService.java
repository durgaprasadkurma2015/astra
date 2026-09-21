package com.astra.inventory.service;

import com.astra.dto.InventoryMovementResponse;
import com.astra.entity.InventoryMovement;
import com.astra.enums.InventoryMovementType;
import com.astra.inventory.dto.InventoryAdjustmentRequest;
import com.astra.inventory.dto.InventoryResponse;
import com.astra.entity.Order;
import com.astra.entity.Product;
import com.astra.entity.ProductInventory;
import com.astra.repository.InventoryMovementRepository;
import com.astra.repository.ProductInventoryRepository;
import com.astra.repository.ProductRepository;

import jakarta.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
public class InventoryService {

    private final ProductInventoryRepository inventoryRepository;
    private final InventoryMovementRepository movementRepository;
    private final ProductRepository productRepository;

    public InventoryService(
            ProductInventoryRepository inventoryRepository,
            InventoryMovementRepository movementRepository,
            ProductRepository productRepository) {

        this.inventoryRepository = inventoryRepository;
        this.movementRepository = movementRepository;
        this.productRepository = productRepository;
    }

    @Transactional
    public InventoryResponse getInventory(Long productId) {

        ProductInventory inventory =
                getOrCreateInventory(productId);

        return toResponse(inventory);
    }

    @Transactional
    public InventoryResponse reserve(
            Long productId,
            int quantity,
            Order order) {

        validateQuantity(quantity);

        ProductInventory inventory =
                getLockedInventory(productId);

        if (inventory.getAvailableQuantity() < quantity) {

            throw new IllegalStateException(
                    "Insufficient inventory for product: "
                            + productId
            );
        }

        int before = inventory.getAvailableQuantity();

        inventory.setAvailableQuantity(
                before - quantity
        );

        inventory.setReservedQuantity(
                inventory.getReservedQuantity() + quantity
        );

        inventoryRepository.save(inventory);

        syncProductStock(inventory);

        createMovement(
                inventory.getProduct(),
                order,
                InventoryMovementType.RESERVE,
                quantity,
                before,
                inventory.getAvailableQuantity(),
                "Stock reserved for order"
        );

        return toResponse(inventory);
    }

    @Transactional
    public InventoryResponse release(
            Long productId,
            int quantity,
            Order order,
            String reason) {

        validateQuantity(quantity);

        ProductInventory inventory =
                getLockedInventory(productId);

        if (inventory.getReservedQuantity() < quantity) {

            throw new IllegalStateException(
                    "Reserved inventory is lower than release quantity"
            );
        }

        int before = inventory.getAvailableQuantity();

        inventory.setReservedQuantity(
                inventory.getReservedQuantity() - quantity
        );

        inventory.setAvailableQuantity(
                before + quantity
        );

        inventoryRepository.save(inventory);

        syncProductStock(inventory);

        createMovement(
                inventory.getProduct(),
                order,
                InventoryMovementType.RELEASE,
                quantity,
                before,
                inventory.getAvailableQuantity(),
                reason
        );

        return toResponse(inventory);
    }

    @Transactional
    public InventoryResponse consume(
            Long productId,
            int quantity,
            Order order) {

        validateQuantity(quantity);

        ProductInventory inventory =
                getLockedInventory(productId);

        if (inventory.getReservedQuantity() < quantity) {

            throw new IllegalStateException(
                    "Reserved inventory is lower than consume quantity"
            );
        }

        int before = inventory.getAvailableQuantity();

        inventory.setReservedQuantity(
                inventory.getReservedQuantity() - quantity
        );

        inventoryRepository.save(inventory);

        Product product = inventory.getProduct();

        product.setSalesCount(
                product.getSalesCount() + quantity
        );

        productRepository.save(product);

        createMovement(
                product,
                order,
                InventoryMovementType.CONSUME,
                quantity,
                before,
                inventory.getAvailableQuantity(),
                "Reserved stock consumed after payment"
        );

        return toResponse(inventory);
    }

    @Transactional
    public InventoryResponse restock(
            Long productId,
            int quantity,
            String reason) {

        validateQuantity(quantity);

        ProductInventory inventory =
                getLockedInventory(productId);

        int before = inventory.getAvailableQuantity();

        inventory.setAvailableQuantity(
                before + quantity
        );

        inventoryRepository.save(inventory);

        syncProductStock(inventory);

        createMovement(
                inventory.getProduct(),
                null,
                InventoryMovementType.RESTOCK,
                quantity,
                before,
                inventory.getAvailableQuantity(),
                reason
        );

        return toResponse(inventory);
    }

    @Transactional
    public InventoryResponse adjust(
            Long productId,
            InventoryAdjustmentRequest request) {

        ProductInventory inventory =
                getLockedInventory(productId);

        int before = inventory.getAvailableQuantity();

        int newQuantity = request.quantity();

        if (newQuantity < 0) {
            throw new IllegalArgumentException(
                    "Inventory quantity cannot be negative"
            );
        }

        inventory.setAvailableQuantity(newQuantity);

        inventoryRepository.save(inventory);

        syncProductStock(inventory);

        int difference =
                Math.abs(newQuantity - before);

        createMovement(
                inventory.getProduct(),
                null,
                InventoryMovementType.ADJUSTMENT,
                difference,
                before,
                newQuantity,
                request.reason()
        );

        return toResponse(inventory);
    }

    public Page<InventoryMovementResponse> getProductMovements(
            Long productId,
            Pageable pageable) {

        return movementRepository
                .findByProductIdOrderByCreatedAtDesc(
                        productId,
                        pageable
                )
                .map(this::toMovementResponse);
    }

    public Page<InventoryMovementResponse> getOrderMovements(
            Long orderId,
            Pageable pageable) {

        return movementRepository
                .findByOrderIdOrderByCreatedAtDesc(
                        orderId,
                        pageable
                )
                .map(this::toMovementResponse);
    }

    private ProductInventory getLockedInventory(
            Long productId) {

        return inventoryRepository
                .findWithLockByProductId(productId)
                .orElseGet(
                        () -> createInventory(productId)
                );
    }

    private ProductInventory getOrCreateInventory(
            Long productId) {

        return inventoryRepository
                .findByProductId(productId)
                .orElseGet(
                        () -> createInventory(productId)
                );
    }

    private ProductInventory createInventory(
            Long productId) {

        Product product =
                productRepository.findById(productId)
                        .orElseThrow(
                                () -> new IllegalArgumentException(
                                        "Product not found: "
                                                + productId
                                )
                        );

        ProductInventory inventory =
                new ProductInventory();

        inventory.setProduct(product);

        inventory.setAvailableQuantity(
                product.getStockQuantity()
        );

        inventory.setReservedQuantity(0);

        return inventoryRepository.save(inventory);
    }

    private void syncProductStock(
            ProductInventory inventory) {

        Product product = inventory.getProduct();

        product.setStockQuantity(
                inventory.getAvailableQuantity()
        );

        productRepository.save(product);
    }

    private void createMovement(
            Product product,
            Order order,
            InventoryMovementType type,
            int quantity,
            int before,
            int after,
            String reason) {

        InventoryMovement movement =
                new InventoryMovement();

        movement.setProduct(product);
        movement.setOrder(order);
        movement.setMovementType(type);
        movement.setQuantity(quantity);
        movement.setQuantityBefore(before);
        movement.setQuantityAfter(after);
        movement.setReason(reason);

        movementRepository.save(movement);
    }

    private void validateQuantity(int quantity) {

        if (quantity <= 0) {
            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }
    }

    private InventoryResponse toResponse(
            ProductInventory inventory) {

        int total =
                inventory.getAvailableQuantity()
                        + inventory.getReservedQuantity();

        return new InventoryResponse(
                inventory.getProduct().getId(),
                inventory.getProduct().getName(),
                inventory.getAvailableQuantity(),
                inventory.getReservedQuantity(),
                total
        );
    }

    private InventoryMovementResponse toMovementResponse(
            InventoryMovement movement) {

        return new InventoryMovementResponse(
                movement.getId(),
                movement.getProduct().getId(),
                movement.getOrder() != null
                        ? movement.getOrder().getId()
                        : null,
                movement.getMovementType(),
                movement.getQuantity(),
                movement.getQuantityBefore(),
                movement.getQuantityAfter(),
                movement.getReason(),
                movement.getCreatedAt()
        );
    }
}