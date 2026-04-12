package com.asal.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderStatusUpdateRequest {

    @NotBlank(message = "Status is required")
    private String status;
}