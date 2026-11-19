
package com.astra.backend.analytics;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/admin/analytics")
public class AnalyticsController {

    private final AnalyticsService analyticsService;

    public AnalyticsController(AnalyticsService analyticsService) {
        this.analyticsService = analyticsService;
    }

    @GetMapping
    public Map<String, Object> getDashboard(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate) {

        return analyticsService.getDashboard(startDate, endDate);
    }

    @GetMapping("/export")
    public ResponseEntity<byte[]> exportOrders(
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate,

            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate endDate) {

        List<Map<String, Object>> orders =
                analyticsService.exportOrders(startDate, endDate);

        StringBuilder csv = new StringBuilder();

        csv.append(
            "Order Number,Customer Name,Customer Email," +
            "Total Amount,Order Status,Payment Status," +
            "Payment Method,Created At\n"
        );

        for (Map<String, Object> order : orders) {

            csv.append(csvValue(order.get("orderNumber"))).append(",");
            csv.append(csvValue(order.get("customerName"))).append(",");
            csv.append(csvValue(order.get("customerEmail"))).append(",");
            csv.append(csvValue(order.get("totalAmount"))).append(",");
            csv.append(csvValue(order.get("orderStatus"))).append(",");
            csv.append(csvValue(order.get("paymentStatus"))).append(",");
            csv.append(csvValue(order.get("paymentMethod"))).append(",");
            csv.append(csvValue(order.get("createdAt"))).append("\n");
        }

        byte[] content = csv.toString()
                .getBytes(StandardCharsets.UTF_8);

        return ResponseEntity.ok()
                .header(
                    HttpHeaders.CONTENT_DISPOSITION,
                    "attachment; filename=astra-orders-analytics.csv"
                )
                .contentType(
                    MediaType.parseMediaType("text/csv")
                )
                .contentLength(content.length)
                .body(content);
    }

    private String csvValue(Object value) {

        if (value == null) {
            return "\"\"";
        }

        String text = String.valueOf(value);

        // Prevent spreadsheet formula injection.
        if (!text.isEmpty() &&
                "=+-@".indexOf(text.charAt(0)) >= 0) {
            text = "'" + text;
        }

        return "\"" + text.replace("\"", "\"\"") + "\"";
    }
}
