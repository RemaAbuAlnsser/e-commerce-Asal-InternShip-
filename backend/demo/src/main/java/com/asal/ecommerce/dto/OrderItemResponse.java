package com.asal.ecommerce.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemResponse {

    private Long id;
    private Long productId;
    private Long productColorId;
    private String productName;
    private BigDecimal productPrice;
    private String colorName;
    private int quantity;
    private BigDecimal subtotal;
    private LocalDateTime createdAt;
}
