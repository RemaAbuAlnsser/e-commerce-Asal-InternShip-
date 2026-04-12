package com.asal.ecommerce.service;

import com.asal.ecommerce.dto.DashboardStatsResponse;
import com.asal.ecommerce.repository.BrandRepository;
import com.asal.ecommerce.repository.CategoryRepository;
import com.asal.ecommerce.repository.OrderRepository;
import com.asal.ecommerce.repository.ProductRepository;
import com.asal.ecommerce.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.WeekFields;
import java.util.Locale;

@Service
@Transactional(readOnly = true)
public class DashboardService {

    @Autowired private ProductRepository  productRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private BrandRepository    brandRepository;
    @Autowired private OrderRepository    orderRepository;
    @Autowired private UserRepository     userRepository;

    public DashboardStatsResponse getStats() {
        LocalDateTime startOfToday   = LocalDate.now().atStartOfDay();
        LocalDateTime startOfWeek    = LocalDate.now()
                .with(WeekFields.of(Locale.getDefault()).dayOfWeek(), 1)
                .atStartOfDay();
        LocalDateTime startOfMonth   = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        return DashboardStatsResponse.builder()
                .totalProducts(productRepository.count())
                .totalCategories(categoryRepository.count())
                .totalBrands(brandRepository.count())
                .totalOrders(orderRepository.count())
                .pendingOrders(orderRepository.countByStatus("pending"))
                .totalUsers(userRepository.count())
                .totalRevenue(orderRepository.sumTotalRevenue())
                .todayRevenue(orderRepository.sumRevenueSince(startOfToday))
                .weeklyRevenue(orderRepository.sumRevenueSince(startOfWeek))
                .monthlyRevenue(orderRepository.sumRevenueSince(startOfMonth))
                .build();
    }
}
