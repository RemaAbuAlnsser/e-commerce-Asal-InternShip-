package com.asal.ecommerce.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class DeliveryCityRequest {

    @NotBlank(message = "City name is required")
    private String cityName;

    @NotNull(message = "Delivery price is required")
    @DecimalMin(value = "0.0", inclusive = true, message = "Delivery price must be greater than or equal to 0")
    private BigDecimal deliveryPrice;
}