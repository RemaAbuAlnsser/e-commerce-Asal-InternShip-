package com.asal.ecommerce.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardStatsResponse {

    // Counts
    private long totalProducts;
    private long totalCategories;
    private long totalBrands;
    private long totalOrders;
    private long pendingOrders;
    private long totalUsers;

    // Revenue
    private BigDecimal totalRevenue;
    private BigDecimal todayRevenue;
    private BigDecimal weeklyRevenue;
    private BigDecimal monthlyRevenue;
}
