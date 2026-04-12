package com.asal.ecommerce.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponse {

    private Long id;
    private Long userId;

    private String customerName;
    private String customerPhone;
    private String customerCity;
    private String customerAddress;

    private String shippingMethod;
    private BigDecimal shippingCost;

    private String paymentMethod;

    private BigDecimal subtotal;
    private BigDecimal total;

    private String status;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    private List<OrderItemResponse> items;
}