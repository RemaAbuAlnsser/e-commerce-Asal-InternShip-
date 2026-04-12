package com.asal.ecommerce.dto;

import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderRequest {

    private Long userId; // nullable for guest

    @NotBlank(message = "Customer name is required")
    private String customerName;

    @NotBlank(message = "Customer phone is required")
    private String customerPhone;

    @NotBlank(message = "Customer city is required")
    private String customerCity;

    @NotBlank(message = "Customer address is required")
    private String customerAddress;

    @NotBlank(message = "Shipping method is required")
    private String shippingMethod;

    @DecimalMin(value = "0.0", inclusive = true, message = "Shipping cost must be >= 0")
    private BigDecimal shippingCost;

    @NotBlank(message = "Payment method is required")
    private String paymentMethod;

    private String status; // optional, default pending

    @Valid
    @NotEmpty(message = "Order must contain at least one item")
    @Builder.Default
    private List<OrderItemRequest> items = new ArrayList<>();
}