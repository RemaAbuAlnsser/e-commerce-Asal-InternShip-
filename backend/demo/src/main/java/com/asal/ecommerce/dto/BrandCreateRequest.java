package com.asal.ecommerce.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class BrandCreateRequest {
    
    @NotBlank(message = "Brand name is required")
    @Size(max = 191, message = "Brand name must not exceed 191 characters")
    private String name;
    
    @Size(max = 500, message = "Logo URL must not exceed 500 characters")
    private String logoUrl;
}
