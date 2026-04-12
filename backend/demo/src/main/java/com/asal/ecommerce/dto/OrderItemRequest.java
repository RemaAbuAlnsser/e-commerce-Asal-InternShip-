package com.asal.ecommerce.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemRequest {

    @NotNull(message = "Product ID is required")
    private Long productId;

    private Long productColorId; // nullable – no color variant selected

    @Min(value = 1, message = "Quantity must be at least 1")
    @Builder.Default
    private int quantity = 1;
}
