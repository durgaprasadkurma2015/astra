package com.astra.service;

import com.astra.admin.dto.AdminDashboardResponse;
import com.astra.repository.OrderRepository;
import com.astra.repository.ProductRepository;
import com.astra.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
@Transactional(readOnly = true)
public class AdminService {

    private final UserRepository userRepository;
    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public AdminService(
            UserRepository userRepository,
            ProductRepository productRepository,
            OrderRepository orderRepository
    ) {
        this.userRepository = userRepository;
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    public AdminDashboardResponse getDashboard() {

        long totalUsers =
                userRepository.count();

        long totalProducts =
                productRepository.count();

        long totalOrders =
                orderRepository.count();

        BigDecimal totalRevenue =
                orderRepository.calculateTotalRevenue();

        long pendingOrders =
                orderRepository.countPendingOrders();

        long lowStockProducts =
                productRepository.countLowStockProducts();

        return new AdminDashboardResponse(
                totalUsers,
                totalProducts,
                totalOrders,
                totalRevenue,
                pendingOrders,
                lowStockProducts
        );
    }
}