package com.asal.ecommerce.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationResponse {
    private Long id;
    private String type;       // "LOW_STOCK" | "OUT_OF_STOCK"
    private String title;
    private String message;
    private String route;
    private LocalDateTime createdAt;
}
