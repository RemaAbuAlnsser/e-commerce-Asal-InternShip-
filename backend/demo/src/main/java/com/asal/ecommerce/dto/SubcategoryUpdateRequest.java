package com.asal.ecommerce.dto;

import lombok.Data;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

@Data
public class SubcategoryUpdateRequest {
    
    @NotBlank(message = "Subcategory name is required")
    @Size(max = 100, message = "Subcategory name must not exceed 100 characters")
    private String name;
    
    private String slug;
    
    @Size(max = 1000, message = "Description must not exceed 1000 characters")
    private String description;
    
    private String imageUrl;
    
    @NotNull(message = "Category ID is required")
    private Long categoryId;
}
