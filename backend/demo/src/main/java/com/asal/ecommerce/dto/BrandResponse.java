package com.asal.ecommerce.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class BrandResponse {
    
    private Long id;
    private String name;
    private String logoUrl;
    private Boolean isActive;
    private LocalDateTime createdAt;
}
