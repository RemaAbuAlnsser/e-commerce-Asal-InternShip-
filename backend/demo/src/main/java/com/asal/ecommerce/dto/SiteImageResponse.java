package com.asal.ecommerce.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SiteImageResponse {

    private Long id;
    private String imageUrl;
    private Integer displayOrder;
    private LocalDateTime createdAt;
}
