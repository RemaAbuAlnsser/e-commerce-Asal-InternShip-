package com.asal.ecommerce.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Data
public class CategoryCreateRequest {
    
    @NotBlank(message = "Category name is required")
    @Size(max = 100, message = "Category name must not exceed 100 characters")
    private String name;
    
    private String slug;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    private String imageUrl;
}
