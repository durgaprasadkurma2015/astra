
package com.astra.backend.inventory;

import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/inventory")
public class InventoryController {

    private final InventoryManagementService inventory;
    private final JdbcTemplate jdbc;

    public InventoryController(
            InventoryManagementService inventory,
            JdbcTemplate jdbc) {
        this.inventory = inventory;
        this.jdbc = jdbc;
    }

    public record TransferRequest(
        @NotNull Long productId,
        @NotNull Long sourceWarehouseId,
        @NotNull Long destinationWarehouseId,
        @NotNull @Positive Integer quantity
    ) {}

    public record WarehouseRequest(
        @jakarta.validation.constraints.NotBlank String warehouseCode,
        @jakarta.validation.constraints.NotBlank String warehouseName,
        String address
    ) {}

    @GetMapping("/warehouses")
    public List<Map<String, Object>> warehouses() {
        return jdbc.queryForList("""
            SELECT
                w.id,
                w.warehouse_code AS warehouseCode,
                w.warehouse_name AS warehouseName,
                w.address,
                w.active,
                COUNT(ws.product_id) AS productCount,
                COALESCE(SUM(ws.on_hand_quantity), 0) AS onHandUnits,
                COALESCE(SUM(ws.reserved_quantity), 0) AS reservedUnits,
                COALESCE(SUM(
                    ws.on_hand_quantity - ws.reserved_quantity
                ), 0) AS availableUnits
            FROM warehouses w
            LEFT JOIN warehouse_stock ws
                ON ws.warehouse_id = w.id
            GROUP BY w.id, w.warehouse_code,
                     w.warehouse_name, w.address, w.active
            ORDER BY w.id
            """);
    }

    @PostMapping("/warehouses")
    public Map<String, Object> createWarehouse(
            @Valid @RequestBody WarehouseRequest request) {

        Long id = jdbc.queryForObject(
            """
            INSERT INTO warehouses (
                warehouse_code, warehouse_name, address
            )
            VALUES (?, ?, ?)
            """,
            Long.class,
            request.warehouseCode().trim().toUpperCase(),
            request.warehouseName().trim(),
            request.address()
        );

        jdbc.update("""
            INSERT INTO warehouse_stock (
                warehouse_id, product_id,
                on_hand_quantity, reserved_quantity, reorder_level
            )
            SELECT ?, id, 0, 0, 5
            FROM products
            """, id);

        return Map.of(
            "id", id,
            "warehouseCode", request.warehouseCode(),
            "warehouseName", request.warehouseName(),
            "message", "Warehouse created successfully"
        );
    }

    @GetMapping("/warehouses/{warehouseId}/stock")
    public List<Map<String, Object>> warehouseStock(
            @PathVariable Long warehouseId) {

        return jdbc.queryForList("""
            SELECT
                p.id AS productId,
                p.name AS productName,
                p.sku,
                p.category,
                ws.on_hand_quantity AS onHandQuantity,
                ws.reserved_quantity AS reservedQuantity,
                (ws.on_hand_quantity - ws.reserved_quantity)
                    AS availableQuantity,
                ws.reorder_level AS reorderLevel
            FROM warehouse_stock ws
            JOIN products p ON p.id = ws.product_id
            WHERE ws.warehouse_id = ?
            ORDER BY p.name
            """, warehouseId);
    }

    @PostMapping("/transfers")
    public Map<String, String> transfer(
            @Valid @RequestBody TransferRequest request,
            Authentication authentication) {

        inventory.transfer(
            request.productId(),
            request.sourceWarehouseId(),
            request.destinationWarehouseId(),
            request.quantity(),
            authentication.getName()
        );

        return Map.of(
            "message",
            "Warehouse stock transfer completed successfully"
        );
    }

    @GetMapping("/forecast")
    public List<Map<String, Object>> forecast(
            @RequestParam(defaultValue = "14") int leadTimeDays) {

        return inventory.forecast(leadTimeDays);
    }
}
