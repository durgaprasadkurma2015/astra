package com.astra.admin.dto;

import java.math.BigDecimal;

public record AdminDashboardResponse(

        long totalUsers,

        long totalProducts,

        long totalOrders,

        BigDecimal totalRevenue,

        long pendingOrders,

        long lowStockProducts

) {
}
