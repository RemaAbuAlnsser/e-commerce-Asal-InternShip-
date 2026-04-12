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
public class CustomerSummaryResponse {

    private Long id;
    private String name;
    private String email;
    private String provider;
    private boolean active;
    private long totalOrders;
    private BigDecimal totalSpent;
}
