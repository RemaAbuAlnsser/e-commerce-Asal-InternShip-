package com.asal.ecommerce.dto;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryCityResponse {

    private Long id;
    private String cityName;
    private BigDecimal deliveryPrice;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}