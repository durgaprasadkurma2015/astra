
package com.astra.backend.analytics;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AnalyticsService {

    private final JdbcTemplate jdbcTemplate;

    private static final DateTimeFormatter DAY_FORMAT =
            DateTimeFormatter.ISO_LOCAL_DATE;

    public AnalyticsService(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public Map<String, Object> getDashboard(
            LocalDate requestedStart,
            LocalDate requestedEnd) {

        LocalDate end = requestedEnd == null
                ? LocalDate.now(ZoneOffset.UTC)
                : requestedEnd;

        LocalDate start = requestedStart == null
                ? end.minusDays(29)
                : requestedStart;

        validateRange(start, end);

        Timestamp from = Timestamp.valueOf(start.atStartOfDay());
        Timestamp to = Timestamp.valueOf(
                end.plusDays(1).atStartOfDay()
        );

        Map<String, Object> result = new LinkedHashMap<>();

        result.put("startDate", start.toString());
        result.put("endDate", end.toString());

        result.put("summary", getSummary(from, to));
        result.put("salesTrend", getSalesTrend(start, end, from, to));
        result.put("paymentMethods", getPaymentMethods(from, to));
        result.put("orderStatuses", getOrderStatuses(from, to));
        result.put("topProducts", getTopProducts(from, to));
        result.put("topCategories", getTopCategories(from, to));
        result.put("lowStockProducts", getLowStockProducts());
        result.put("recentOrders", getRecentOrders(from, to));

        return result;
    }

    private void validateRange(LocalDate start, LocalDate end) {

        if (start.isAfter(end)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid analytics date range."
            );
        }

        if (start.plusDays(365).isBefore(end)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid analytics date range."
            );
        }
    }

    private Map<String, Object> getSummary(
            Timestamp from,
            Timestamp to) {

        Map<String, Object> summary = jdbcTemplate.queryForMap("""
            SELECT
                COUNT(*) AS totalOrders,

                COALESCE(SUM(
                    CASE
                        WHEN UPPER(order_status) <> 'CANCELLED'
                        THEN total_amount
                        ELSE 0
                    END
                ), 0) AS grossSales,

                COALESCE(SUM(
                    CASE
                        WHEN UPPER(payment_status) = 'PAID'
                         AND UPPER(order_status) <> 'CANCELLED'
                        THEN total_amount
                        ELSE 0
                    END
                ), 0) AS collectedRevenue,

                COALESCE(AVG(
                    CASE
                        WHEN UPPER(order_status) <> 'CANCELLED'
                        THEN total_amount
                    END
                ), 0) AS averageOrderValue,

                COUNT(DISTINCT CASE
                    WHEN UPPER(order_status) <> 'CANCELLED'
                    THEN user_email
                END) AS purchasingCustomers,

                COALESCE(SUM(
                    CASE
                        WHEN UPPER(order_status) = 'CANCELLED'
                        THEN 1 ELSE 0
                    END
                ), 0) AS cancelledOrders

            FROM customer_orders
            WHERE created_at >= ?
              AND created_at < ?
            """, from, to);

        summary.put(
                "totalProducts",
                jdbcTemplate.queryForObject(
                        "SELECT COUNT(*) FROM products WHERE active = TRUE",
                        Long.class
                )
        );

        summary.put(
                "totalCustomers",
                jdbcTemplate.queryForObject("""
                    SELECT COUNT(*)
                    FROM app_users
                    WHERE role = 'CUSTOMER'
                    """, Long.class)
        );

        summary.put(
                "newCustomers",
                jdbcTemplate.queryForObject("""
                    SELECT COUNT(*)
                    FROM app_users
                    WHERE role = 'CUSTOMER'
                      AND created_at >= ?
                      AND created_at < ?
                    """, Long.class, from, to)
        );

        summary.put(
                "lowStockCount",
                jdbcTemplate.queryForObject("""
                    SELECT COUNT(*)
                    FROM products
                    WHERE active = TRUE
                      AND stock_quantity <= 5
                    """, Long.class)
        );

        return summary;
    }

    private List<Map<String, Object>> getSalesTrend(
            LocalDate start,
            LocalDate end,
            Timestamp from,
            Timestamp to) {

        List<Map<String, Object>> databaseRows =
                jdbcTemplate.queryForList("""
                    SELECT
                        DATE(created_at) AS day,

                        COUNT(*) AS orderCount,

                        COALESCE(SUM(
                            CASE
                                WHEN UPPER(order_status) <> 'CANCELLED'
                                THEN total_amount ELSE 0
                            END
                        ), 0) AS grossSales,

                        COALESCE(SUM(
                            CASE
                                WHEN UPPER(payment_status) = 'PAID'
                                 AND UPPER(order_status) <> 'CANCELLED'
                                THEN total_amount ELSE 0
                            END
                        ), 0) AS collectedRevenue

                    FROM customer_orders
                    WHERE created_at >= ?
                      AND created_at < ?

                    GROUP BY DATE(created_at)
                    ORDER BY DATE(created_at)
                    """, from, to);

        Map<String, Map<String, Object>> indexed =
                new LinkedHashMap<>();

        for (Map<String, Object> row : databaseRows) {
            String dateKey = String.valueOf(row.get("day"));

            indexed.put(dateKey, row);
        }

        List<Map<String, Object>> result = new ArrayList<>();

        for (LocalDate day = start;
                !day.isAfter(end);
                day = day.plusDays(1)) {

            String key = day.format(DAY_FORMAT);
            Map<String, Object> existing = indexed.get(key);

            Map<String, Object> point = new LinkedHashMap<>();

            point.put("day", key);

            point.put(
                    "orderCount",
                    existing == null ? 0 : existing.get("orderCount")
            );

            point.put(
                    "grossSales",
                    existing == null
                            ? BigDecimal.ZERO
                            : existing.get("grossSales")
            );

            point.put(
                    "collectedRevenue",
                    existing == null
                            ? BigDecimal.ZERO
                            : existing.get("collectedRevenue")
            );

            result.add(point);
        }

        return result;
    }

    private List<Map<String, Object>> getPaymentMethods(
            Timestamp from,
            Timestamp to) {

        return jdbcTemplate.queryForList("""
            SELECT
                payment_method AS method,
                COUNT(*) AS orderCount,

                COALESCE(SUM(
                    CASE
                        WHEN UPPER(payment_status) = 'PAID'
                         AND UPPER(order_status) <> 'CANCELLED'
                        THEN total_amount
                        ELSE 0
                    END
                ), 0) AS collectedRevenue

            FROM customer_orders
            WHERE created_at >= ?
              AND created_at < ?

            GROUP BY payment_method
            ORDER BY orderCount DESC
            """, from, to);
    }

    private List<Map<String, Object>> getOrderStatuses(
            Timestamp from,
            Timestamp to) {

        return jdbcTemplate.queryForList("""
            SELECT
                order_status AS status,
                COUNT(*) AS orderCount

            FROM customer_orders
            WHERE created_at >= ?
              AND created_at < ?

            GROUP BY order_status
            ORDER BY orderCount DESC
            """, from, to);
    }

    private List<Map<String, Object>> getTopProducts(
            Timestamp from,
            Timestamp to) {

        return jdbcTemplate.queryForList("""
            SELECT
                oi.product_id AS productId,
                oi.product_name AS productName,
                SUM(oi.quantity) AS unitsSold,
                SUM(oi.line_total) AS salesAmount

            FROM customer_order_items oi

            INNER JOIN customer_orders o
                ON o.id = oi.order_id

            WHERE o.created_at >= ?
              AND o.created_at < ?
              AND UPPER(o.order_status) <> 'CANCELLED'

            GROUP BY oi.product_id, oi.product_name

            ORDER BY unitsSold DESC, salesAmount DESC

            LIMIT 10
            """, from, to);
    }

    private List<Map<String, Object>> getTopCategories(
            Timestamp from,
            Timestamp to) {

        return jdbcTemplate.queryForList("""
            SELECT
                COALESCE(p.category, 'Uncategorized') AS category,
                SUM(oi.quantity) AS unitsSold,
                SUM(oi.line_total) AS salesAmount

            FROM customer_order_items oi

            INNER JOIN customer_orders o
                ON o.id = oi.order_id

            INNER JOIN products p
                ON p.id = oi.product_id

            WHERE o.created_at >= ?
              AND o.created_at < ?
              AND UPPER(o.order_status) <> 'CANCELLED'

            GROUP BY COALESCE(p.category, 'Uncategorized')

            ORDER BY salesAmount DESC

            LIMIT 10
            """, from, to);
    }

    private List<Map<String, Object>> getLowStockProducts() {

        return jdbcTemplate.queryForList("""
            SELECT
                id,
                name,
                sku,
                category,
                stock_quantity AS stockQuantity

            FROM products

            WHERE active = TRUE
              AND stock_quantity <= 5

            ORDER BY stock_quantity ASC, name ASC

            LIMIT 10
            """);
    }

    private List<Map<String, Object>> getRecentOrders(
            Timestamp from,
            Timestamp to) {

        return jdbcTemplate.queryForList("""
            SELECT
                order_number AS orderNumber,
                customer_name AS customerName,
                total_amount AS totalAmount,
                order_status AS orderStatus,
                payment_status AS paymentStatus,
                payment_method AS paymentMethod,
                created_at AS createdAt

            FROM customer_orders

            WHERE created_at >= ?
              AND created_at < ?

            ORDER BY created_at DESC

            LIMIT 8
            """, from, to);
    }

    public List<Map<String, Object>> exportOrders(
            LocalDate requestedStart,
            LocalDate requestedEnd) {

        LocalDate end = requestedEnd == null
                ? LocalDate.now(ZoneOffset.UTC)
                : requestedEnd;

        LocalDate start = requestedStart == null
                ? end.minusDays(29)
                : requestedStart;

        validateRange(start, end);

        Timestamp from = Timestamp.valueOf(start.atStartOfDay());
        Timestamp to = Timestamp.valueOf(
                end.plusDays(1).atStartOfDay()
        );

        return jdbcTemplate.queryForList("""
            SELECT
                order_number AS orderNumber,
                customer_name AS customerName,
                user_email AS customerEmail,
                total_amount AS totalAmount,
                order_status AS orderStatus,
                payment_status AS paymentStatus,
                payment_method AS paymentMethod,
                created_at AS createdAt

            FROM customer_orders

            WHERE created_at >= ?
              AND created_at < ?

            ORDER BY created_at DESC
            """, from, to);
    }
}
