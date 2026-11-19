
package com.astra.backend.inventory;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

@Service
public class InventoryManagementService {

    private final JdbcTemplate jdbc;

    public InventoryManagementService(JdbcTemplate jdbc) {
        this.jdbc = jdbc;
    }

    public record ReservationItem(Long productId, Integer quantity) {}

    public record ReservationResult(
        String reservationId,
        String status,
        LocalDateTime expiresAt
    ) {}

    private record StockRow(int onHand, int reserved) {}

    private StockRow lockStock(Long warehouseId, Long productId) {

        List<StockRow> rows = jdbc.query(
            """
            SELECT on_hand_quantity, reserved_quantity
            FROM warehouse_stock
            WHERE warehouse_id = ? AND product_id = ?
            FOR UPDATE
            """,
            (rs, rowNum) -> new StockRow(
                rs.getInt("on_hand_quantity"),
                rs.getInt("reserved_quantity")
            ),
            warehouseId,
            productId
        );

        if (rows.isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Warehouse stock not found for product " + productId
            );
        }

        return rows.get(0);
    }

    @Transactional
    public ReservationResult reserve(
        String referenceType,
        String referenceId,
        String customerEmail,
        Long warehouseId,
        List<ReservationItem> items,
        int expiryMinutes
    ) {

        if (items == null || items.isEmpty()
                || expiryMinutes < 1 || expiryMinutes > 1440) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Invalid reservation request"
            );
        }

        if (!List.of("ORDER", "CHECKOUT").contains(referenceType)) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Unsupported reservation reference type"
            );
        }

        if (referenceId == null || referenceId.isBlank()
                || customerEmail == null || customerEmail.isBlank()
                || warehouseId == null) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Reference, customer email and warehouse are required"
            );
        }

        String normalizedEmail = customerEmail.trim()
            .toLowerCase(java.util.Locale.ROOT);

        // Validate items before sorting.
        if (items.stream().anyMatch(item ->
                item == null
                || item.productId() == null
                || item.quantity() == null
                || item.quantity() <= 0)) {

            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Product IDs and positive quantities are required"
            );
        }

        // Lock products in ascending ID order.
        List<ReservationItem> sorted = items.stream()
            .sorted((a, b) ->
                a.productId().compareTo(b.productId()))
            .toList();

        if (sorted.stream().map(ReservationItem::productId)
                .distinct().count() != sorted.size()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Duplicate products are not allowed"
            );
        }

        List<Map<String, Object>> existing = jdbc.queryForList(
            """
            SELECT id, status, expires_at
            FROM inventory_reservations
            WHERE reference_type = ? AND reference_id = ?
            """,
            referenceType,
            referenceId
        );

        if (!existing.isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "A reservation already exists for this reference"
            );
        }

        String reservationId = UUID.randomUUID().toString();

        LocalDateTime expiry =
            LocalDateTime.now(ZoneOffset.UTC).plusMinutes(expiryMinutes);

        // Validate and lock all stock before updating.
        for (ReservationItem item : sorted) {

            StockRow stock = lockStock(
                warehouseId,
                item.productId()
            );

            int available = stock.onHand() - stock.reserved();

            if (available < item.quantity()) {
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Insufficient available stock for product "
                        + item.productId()
                );
            }
        }

        jdbc.update(
            """
            INSERT INTO inventory_reservations (
                id, reference_type, reference_id,
                customer_email, status, expires_at
            )
            VALUES (?, ?, ?, ?, 'ACTIVE', ?)
            """,
            reservationId,
            referenceType,
            referenceId,
            normalizedEmail,
            Timestamp.valueOf(expiry)
        );

        for (ReservationItem item : sorted) {

            jdbc.update(
                """
                UPDATE warehouse_stock
                SET reserved_quantity = reserved_quantity + ?
                WHERE warehouse_id = ? AND product_id = ?
                """,
                item.quantity(),
                warehouseId,
                item.productId()
            );

            // Product stock_quantity is the storefront's
            // available-stock mirror.
            int updated = jdbc.update(
                """
                UPDATE products
                SET stock_quantity = stock_quantity - ?
                WHERE id = ? AND stock_quantity >= ?
                """,
                item.quantity(),
                item.productId(),
                item.quantity()
            );

            if (updated != 1) {
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Product availability changed during reservation"
                );
            }

            jdbc.update(
                """
                INSERT INTO inventory_reservation_items (
                    reservation_id, warehouse_id,
                    product_id, quantity
                )
                VALUES (?, ?, ?, ?)
                """,
                reservationId,
                warehouseId,
                item.productId(),
                item.quantity()
            );
        }

        return new ReservationResult(
            reservationId,
            "ACTIVE",
            expiry
        );
    }

    @Transactional
    public void commit(String reservationId) {

        Map<String, Object> reservation =
            lockReservation(reservationId);

        String status = String.valueOf(reservation.get("status"));

        if ("COMMITTED".equals(status)) {
            return;
        }

        if (!"ACTIVE".equals(status)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Only active reservations can be committed"
            );
        }

        // Prevent committing an expired reservation.
        Timestamp expiryTimestamp =
            (Timestamp) reservation.get("expires_at");

        LocalDateTime expiresAt =
            expiryTimestamp.toLocalDateTime();

        LocalDateTime now =
            LocalDateTime.now(ZoneOffset.UTC);

        if (!now.isBefore(expiresAt)) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Reservation has expired and cannot be committed"
            );
        }

        List<Map<String, Object>> items = jdbc.queryForList(
            """
            SELECT warehouse_id, product_id, quantity
            FROM inventory_reservation_items
            WHERE reservation_id = ?
            ORDER BY product_id
            """,
            reservationId
        );

        for (Map<String, Object> item : items) {

            Long warehouseId =
                ((Number) item.get("warehouse_id")).longValue();

            Long productId =
                ((Number) item.get("product_id")).longValue();

            int quantity =
                ((Number) item.get("quantity")).intValue();

            lockStock(warehouseId, productId);

            int updated = jdbc.update(
                """
                UPDATE warehouse_stock
                SET on_hand_quantity = on_hand_quantity - ?,
                    reserved_quantity = reserved_quantity - ?
                WHERE warehouse_id = ?
                  AND product_id = ?
                  AND on_hand_quantity >= ?
                  AND reserved_quantity >= ?
                """,
                quantity,
                quantity,
                warehouseId,
                productId,
                quantity,
                quantity
            );

            if (updated != 1) {
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Unable to commit reserved inventory"
                );
            }

            // Do not change products.stock_quantity here:
            // it was already reduced at reservation time.
        }

        jdbc.update(
            """
            UPDATE inventory_reservations
            SET status = 'COMMITTED',
                completed_at = UTC_TIMESTAMP()
            WHERE id = ? AND status = 'ACTIVE'
            """,
            reservationId
        );
    }

    /**
     * Releases a reservation using its business reference.
     * Used by OrderService during order cancellation.
     */
    @Transactional
    public void releaseByReference(
        String referenceType,
        String referenceId
    ) {

        if (referenceType == null || referenceType.isBlank()
                || referenceId == null || referenceId.isBlank()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Reservation reference type and ID are required"
            );
        }

        List<Map<String, Object>> rows = jdbc.queryForList(
            """
            SELECT id
            FROM inventory_reservations
            WHERE reference_type = ?
              AND reference_id = ?
            FOR UPDATE
            """,
            referenceType,
            referenceId
        );

        if (rows.isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "No inventory reservation found for this order"
            );
        }

        String reservationId =
            String.valueOf(rows.get(0).get("id"));

        release(reservationId, false);
    }

    /**
     * Releases ACTIVE reservations or restores COMMITTED reservations.
     *
     * ACTIVE:
     * - Decrease reserved quantity.
     * - Restore product available stock.
     * - Do not change warehouse on-hand quantity.
     *
     * COMMITTED:
     * - Restore warehouse on-hand quantity.
     * - Restore product available stock.
     * - Do not change reserved quantity.
     */
    @Transactional
    public void release(String reservationId, boolean expired) {

        Map<String, Object> reservation =
            lockReservation(reservationId);

        String status = String.valueOf(reservation.get("status"));

        // Already released: do nothing.
        if ("RELEASED".equals(status) || "EXPIRED".equals(status)) {
            return;
        }

        boolean active = "ACTIVE".equals(status);
        boolean committed = "COMMITTED".equals(status);

        if (!active && !committed) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Reservation cannot be released from status: " + status
            );
        }

        // A committed reservation cannot be expired.
        if (committed && expired) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "A committed reservation cannot be expired"
            );
        }

        List<Map<String, Object>> items = jdbc.queryForList(
            """
            SELECT warehouse_id, product_id, quantity
            FROM inventory_reservation_items
            WHERE reservation_id = ?
            ORDER BY product_id
            """,
            reservationId
        );

        for (Map<String, Object> item : items) {

            Long warehouseId =
                ((Number) item.get("warehouse_id")).longValue();

            Long productId =
                ((Number) item.get("product_id")).longValue();

            int quantity =
                ((Number) item.get("quantity")).intValue();

            lockStock(warehouseId, productId);

            int updated;

            if (active) {

                // Active reservation:
                // release reserved stock; on-hand remains unchanged.
                updated = jdbc.update(
                    """
                    UPDATE warehouse_stock
                    SET reserved_quantity = reserved_quantity - ?
                    WHERE warehouse_id = ?
                      AND product_id = ?
                      AND reserved_quantity >= ?
                    """,
                    quantity,
                    warehouseId,
                    productId,
                    quantity
                );

            } else {

                // Committed reservation:
                // restore stock physically consumed by the order.
                // reserved_quantity must not be changed.
                updated = jdbc.update(
                    """
                    UPDATE warehouse_stock
                    SET on_hand_quantity = on_hand_quantity + ?
                    WHERE warehouse_id = ?
                      AND product_id = ?
                    """,
                    quantity,
                    warehouseId,
                    productId
                );
            }

            if (updated != 1) {
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Unable to restore warehouse stock for product "
                        + productId
                );
            }

            int productUpdated = jdbc.update(
                """
                UPDATE products
                SET stock_quantity = stock_quantity + ?
                WHERE id = ?
                """,
                quantity,
                productId
            );

            if (productUpdated != 1) {
                throw new ResponseStatusException(
                    HttpStatus.CONFLICT,
                    "Unable to restore product stock for product "
                        + productId
                );
            }
        }

        String newStatus = expired ? "EXPIRED" : "RELEASED";

        int statusUpdated = jdbc.update(
            """
            UPDATE inventory_reservations
            SET status = ?,
                completed_at = UTC_TIMESTAMP()
            WHERE id = ? AND status = ?
            """,
            newStatus,
            reservationId,
            status
        );

        if (statusUpdated != 1) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Reservation status changed during release"
            );
        }
    }

    private Map<String, Object> lockReservation(String id) {

        List<Map<String, Object>> rows = jdbc.queryForList(
            """
            SELECT id, status, expires_at
            FROM inventory_reservations
            WHERE id = ?
            FOR UPDATE
            """,
            id
        );

        if (rows.isEmpty()) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Reservation not found"
            );
        }

        return rows.get(0);
    }

    @Transactional
    public void transfer(
        Long productId,
        Long sourceWarehouse,
        Long destinationWarehouse,
        int quantity,
        String actorEmail
    ) {

        if (productId == null || sourceWarehouse == null
                || destinationWarehouse == null
                || sourceWarehouse.equals(destinationWarehouse)
                || quantity <= 0
                || actorEmail == null || actorEmail.isBlank()) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Invalid warehouse transfer"
            );
        }

        // Lock warehouse stock rows in stable order.
        List<Long> warehouseIds = new ArrayList<>(
            List.of(sourceWarehouse, destinationWarehouse)
        );

        warehouseIds.sort(Long::compareTo);

        for (Long warehouseId : warehouseIds) {
            lockStock(warehouseId, productId);
        }

        int deducted = jdbc.update(
            """
            UPDATE warehouse_stock
            SET on_hand_quantity = on_hand_quantity - ?
            WHERE warehouse_id = ?
              AND product_id = ?
              AND (on_hand_quantity - reserved_quantity) >= ?
            """,
            quantity,
            sourceWarehouse,
            productId,
            quantity
        );

        if (deducted != 1) {
            throw new ResponseStatusException(
                HttpStatus.CONFLICT,
                "Insufficient available stock in source warehouse"
            );
        }

        int added = jdbc.update(
            """
            UPDATE warehouse_stock
            SET on_hand_quantity = on_hand_quantity + ?
            WHERE warehouse_id = ? AND product_id = ?
            """,
            quantity,
            destinationWarehouse,
            productId
        );

        if (added != 1) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Product is not configured in destination warehouse"
            );
        }

        String transferNumber = "TRF-" +
            UUID.randomUUID().toString()
                .replace("-", "")
                .substring(0, 12)
                .toUpperCase();

        jdbc.update(
            """
            INSERT INTO warehouse_transfers (
                transfer_number, product_id,
                source_warehouse_id, destination_warehouse_id,
                quantity, status, actor_email
            )
            VALUES (?, ?, ?, ?, ?, 'COMPLETED', ?)
            """,
            transferNumber,
            productId,
            sourceWarehouse,
            destinationWarehouse,
            quantity,
            actorEmail.trim().toLowerCase(
                java.util.Locale.ROOT
            )
        );

        // Total available stock across all warehouses is unchanged.
        // Product.stock_quantity is intentionally not changed.
    }

    @Transactional(readOnly = true)
    public List<Map<String, Object>> forecast(int leadTimeDays) {

        if (leadTimeDays < 1 || leadTimeDays > 180) {
            throw new ResponseStatusException(
                HttpStatus.BAD_REQUEST,
                "Lead time must be between 1 and 180 days"
            );
        }

        return jdbc.queryForList(
            """
            SELECT
                p.id AS productId,
                p.name AS productName,
                p.sku AS sku,
                p.stock_quantity AS availableStock,
                COALESCE(d.unitsSold, 0) AS unitsSoldLast30Days,

                ROUND(COALESCE(d.unitsSold, 0) / 30.0, 2)
                    AS averageDailyDemand,

                CASE
                    WHEN COALESCE(d.unitsSold, 0) = 0 THEN NULL
                    ELSE ROUND(
                        p.stock_quantity /
                        (d.unitsSold / 30.0), 1
                    )
                END AS estimatedDaysUntilStockout,

                GREATEST(
                    0,
                    CEIL(
                        (COALESCE(d.unitsSold, 0) / 30.0)
                        * (? + 7) - p.stock_quantity
                    )
                ) AS suggestedReorderQuantity

            FROM products p

            LEFT JOIN (
                SELECT
                    oi.product_id,
                    SUM(oi.quantity) AS unitsSold
                FROM customer_order_items oi
                INNER JOIN customer_orders o
                    ON o.id = oi.order_id
                WHERE o.created_at >=
                    DATE_SUB(UTC_TIMESTAMP(), INTERVAL 30 DAY)
                  AND UPPER(o.order_status) <> 'CANCELLED'
                GROUP BY oi.product_id
            ) d ON d.product_id = p.id

            WHERE p.active = TRUE

            ORDER BY
                CASE
                    WHEN d.unitsSold IS NULL OR d.unitsSold = 0
                    THEN 1 ELSE 0
                END,
                estimatedDaysUntilStockout ASC
            """,
            leadTimeDays
        );
    }
}
