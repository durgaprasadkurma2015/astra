package com.astra.inventory.service;

import com.astra.dto.InventoryMovementResponse;
import com.astra.entity.InventoryMovement;
import com.astra.entity.Order;
import com.astra.entity.Product;
import com.astra.entity.ProductInventory;
import com.astra.enums.InventoryMovementType;
import com.astra.inventory.dto.InventoryAdjustmentRequest;
import com.astra.inventory.dto.InventoryResponse;
import com.astra.repository.InventoryMovementRepository;
import com.astra.repository.ProductInventoryRepository;
import com.astra.repository.ProductRepository;

import jakarta.transaction.Transactional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class InventoryService {

    private final ProductInventoryRepository inventoryRepository;
    private final InventoryMovementRepository movementRepository;
    private final ProductRepository productRepository;

    public InventoryService(
            ProductInventoryRepository inventoryRepository,
            InventoryMovementRepository movementRepository,
            ProductRepository productRepository
    ) {

        this.inventoryRepository = inventoryRepository;
        this.movementRepository = movementRepository;
        this.productRepository = productRepository;
    }

    /**
     * Get inventory for a product.
     *
     * If inventory does not exist yet, it is created
     * using the current Product.stockQuantity.
     */
    public InventoryResponse getInventory(
            Long productId
    ) {

        ProductInventory inventory =
                getOrCreateInventory(productId);

        return toResponse(inventory);
    }

    /**
     * Reserve available inventory for an order.
     *
     * available -> reserved
     */
    public InventoryResponse reserve(
            Long productId,
            int quantity,
            Order order
    ) {

        validateQuantity(quantity);

        ProductInventory inventory =
                getLockedInventory(productId);

        int available =
                safeInt(
                        inventory.getAvailableQuantity()
                );

        if (available < quantity) {

            throw new IllegalStateException(
                    "Insufficient inventory for product: "
                            + productId
            );
        }

        int beforeAvailable = available;

        int beforeReserved =
                safeInt(
                        inventory.getReservedQuantity()
                );

        inventory.setAvailableQuantity(
                available - quantity
        );

        inventory.setReservedQuantity(
                beforeReserved + quantity
        );

        inventoryRepository.save(inventory);

        syncProductStock(inventory);

        createMovement(
                inventory.getProduct(),
                order,
                InventoryMovementType.RESERVE,
                quantity,
                beforeAvailable,
                inventory.getAvailableQuantity(),
                "Stock reserved for order"
        );

        return toResponse(inventory);
    }

    /**
     * Release reserved inventory.
     *
     * reserved -> available
     *
     * Used when an order is cancelled before consumption.
     */
    public InventoryResponse release(
            Long productId,
            int quantity,
            Order order,
            String reason
    ) {

        validateQuantity(quantity);

        ProductInventory inventory =
                getLockedInventory(productId);

        int reserved =
                safeInt(
                        inventory.getReservedQuantity()
                );

        if (reserved < quantity) {

            throw new IllegalStateException(
                    "Reserved inventory is lower than release quantity"
            );
        }

        int beforeAvailable =
                safeInt(
                        inventory.getAvailableQuantity()
                );

        inventory.setReservedQuantity(
                reserved - quantity
        );

        inventory.setAvailableQuantity(
                beforeAvailable + quantity
        );

        inventoryRepository.save(inventory);

        syncProductStock(inventory);

        createMovement(
                inventory.getProduct(),
                order,
                InventoryMovementType.RELEASE,
                quantity,
                beforeAvailable,
                inventory.getAvailableQuantity(),
                reason != null && !reason.isBlank()
                        ? reason
                        : "Reserved stock released"
        );

        return toResponse(inventory);
    }

    /**
     * Consume reserved inventory.
     *
     * reserved -> consumed
     *
     * Available quantity does not change because
     * the stock was already removed from available
     * quantity during reservation.
     */
    public InventoryResponse consume(
            Long productId,
            int quantity,
            Order order
    ) {

        validateQuantity(quantity);

        ProductInventory inventory =
                getLockedInventory(productId);

        int reserved =
                safeInt(
                        inventory.getReservedQuantity()
                );

        if (reserved < quantity) {

            throw new IllegalStateException(
                    "Reserved inventory is lower than consume quantity"
            );
        }

        int beforeAvailable =
                safeInt(
                        inventory.getAvailableQuantity()
                );

        int beforeReserved = reserved;

        inventory.setReservedQuantity(
                reserved - quantity
        );

        inventoryRepository.save(inventory);

        /*
         * Sales count increases only when the reserved
         * inventory is actually consumed.
         */
        Product product =
                inventory.getProduct();

        long currentSales =
                safeLong(
                        product.getSalesCount()
                );

        product.setSalesCount(
                currentSales + quantity
        );

        productRepository.save(product);

        createMovement(
                product,
                order,
                InventoryMovementType.CONSUME,
                quantity,
                beforeAvailable,
                inventory.getAvailableQuantity(),
                "Reserved stock consumed after successful payment"
        );

        return toResponse(inventory);
    }

    /**
     * Restock inventory.
     *
     * Adds quantity to available stock.
     */
    public InventoryResponse restock(
            Long productId,
            int quantity,
            String reason
    ) {

        validateQuantity(quantity);

        ProductInventory inventory =
                getLockedInventory(productId);

        int before =
                safeInt(
                        inventory.getAvailableQuantity()
                );

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
                reason != null && !reason.isBlank()
                        ? reason
                        : "Inventory restocked"
        );

        return toResponse(inventory);
    }

    /**
     * Set available inventory to an exact quantity.
     *
     * Reserved inventory is not changed.
     */
    public InventoryResponse adjust(
            Long productId,
            InventoryAdjustmentRequest request
    ) {

        ProductInventory inventory =
                getLockedInventory(productId);

        int newQuantity =
                request.quantity();

        if (newQuantity < 0) {

            throw new IllegalArgumentException(
                    "Inventory quantity cannot be negative"
            );
        }

        int before =
                safeInt(
                        inventory.getAvailableQuantity()
                );

        inventory.setAvailableQuantity(
                newQuantity
        );

        inventoryRepository.save(inventory);

        syncProductStock(inventory);

        int difference =
                Math.abs(
                        newQuantity - before
                );

        /*
         * Do not create a zero-quantity movement.
         */
        if (difference > 0) {

            createMovement(
                    inventory.getProduct(),
                    null,
                    InventoryMovementType.ADJUSTMENT,
                    difference,
                    before,
                    newQuantity,
                    request.reason()
            );
        }

        return toResponse(inventory);
    }

    /**
     * Get inventory movements for a product.
     */
    @Transactional
    public Page<InventoryMovementResponse> getProductMovements(
            Long productId,
            Pageable pageable
    ) {

        return movementRepository
                .findByProductIdOrderByCreatedAtDesc(
                        productId,
                        pageable
                )
                .map(this::toMovementResponse);
    }

    /**
     * Get inventory movements for an order.
     */
    @Transactional
    public Page<InventoryMovementResponse> getOrderMovements(
            Long orderId,
            Pageable pageable
    ) {

        return movementRepository
                .findByOrderIdOrderByCreatedAtDesc(
                        orderId,
                        pageable
                )
                .map(this::toMovementResponse);
    }

    /**
     * Find inventory with a database lock.
     *
     * This protects against two simultaneous orders
     * reserving the same stock.
     */
    private ProductInventory getLockedInventory(
            Long productId
    ) {

        return inventoryRepository
                .findWithLockByProductId(productId)
                .orElseGet(
                        () -> createInventory(productId)
                );
    }

    /**
     * Get existing inventory or create it.
     */
    private ProductInventory getOrCreateInventory(
            Long productId
    ) {

        return inventoryRepository
                .findByProductId(productId)
                .orElseGet(
                        () -> createInventory(productId)
                );
    }

    /**
     * Create inventory using the Product's current
     * stock quantity.
     */
    private ProductInventory createInventory(
            Long productId
    ) {

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
                safeInt(
                        product.getStockQuantity()
                )
        );

        inventory.setReservedQuantity(0);

        return inventoryRepository.save(inventory);
    }

    /**
     * Keep Product.stockQuantity synchronized with
     * available inventory.
     */
    private void syncProductStock(
            ProductInventory inventory
    ) {

        Product product =
                inventory.getProduct();

        product.setStockQuantity(
                safeInt(
                        inventory.getAvailableQuantity()
                )
        );

        productRepository.save(product);
    }

    /**
     * Create inventory movement record.
     */
    private void createMovement(
            Product product,
            Order order,
            InventoryMovementType type,
            int quantity,
            int before,
            int after,
            String reason
    ) {

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

    /**
     * Validate inventory quantity.
     */
    private void validateQuantity(
            int quantity
    ) {

        if (quantity <= 0) {

            throw new IllegalArgumentException(
                    "Quantity must be greater than zero"
            );
        }
    }

    /**
     * Convert inventory entity to API response.
     */
    private InventoryResponse toResponse(
            ProductInventory inventory
    ) {

        int available =
                safeInt(
                        inventory.getAvailableQuantity()
                );

        int reserved =
                safeInt(
                        inventory.getReservedQuantity()
                );

        int total =
                available + reserved;

        return new InventoryResponse(
                inventory.getProduct().getId(),
                inventory.getProduct().getName(),
                available,
                reserved,
                total
        );
    }

    /**
     * Convert movement entity to API response.
     */
    private InventoryMovementResponse toMovementResponse(
            InventoryMovement movement
    ) {

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

    private int safeInt(
            Integer value
    ) {

        return value == null ? 0 : value;
    }

    private long safeLong(
            Long value
    ) {

        return value == null ? 0L : value;
    }
}
