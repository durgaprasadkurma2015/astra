package com.astra.seller.dto;

import java.math.BigDecimal;

public record SellerDashboardResponse(

        long totalProducts,

        long activeProducts,

        long lowStockProducts,

        long totalOrders,

        long pendingOrders,

        long deliveredOrders,

        BigDecimal totalSales,

        BigDecimal pendingPayout

) {
}