package com.asal.ecommerce.mapper;

import com.asal.ecommerce.dto.*;
import com.asal.ecommerce.model.Category;
import com.asal.ecommerce.model.Subcategory;
import org.springframework.stereotype.Component;

@Component
public class SubcategoryMapper {
    
    public Subcategory toEntity(SubcategoryCreateRequest request, Category category) {
        Subcategory subcategory = new Subcategory();
        subcategory.setName(request.getName().trim());
        subcategory.setSlug(generateSlug(request.getSlug(), request.getName().trim()));
        subcategory.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        subcategory.setImageUrl(request.getImageUrl());
        subcategory.setIsActive(true);
        subcategory.setCategory(category);
        return subcategory;
    }
    
    public SubcategoryResponse toResponse(Subcategory subcategory) {
        SubcategoryResponse response = new SubcategoryResponse();
        response.setId(subcategory.getId());
        response.setName(subcategory.getName());
        response.setSlug(subcategory.getSlug());
        response.setDescription(subcategory.getDescription());
        response.setImageUrl(subcategory.getImageUrl());
        response.setIsActive(subcategory.getIsActive());
        response.setCreatedAt(subcategory.getCreatedAt());
        response.setUpdatedAt(subcategory.getUpdatedAt());
        response.setCategoryId(subcategory.getCategory().getId());
        response.setCategoryName(subcategory.getCategory().getName());
        return response;
    }
    
    public void updateEntity(Subcategory subcategory, SubcategoryUpdateRequest request, Category category) {
        subcategory.setName(request.getName().trim());
        subcategory.setSlug(generateSlug(request.getSlug(), request.getName().trim()));
        subcategory.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        subcategory.setImageUrl(request.getImageUrl());
        subcategory.setCategory(category);
    }
    
    public String generateSlug(String providedSlug, String name) {
        if (providedSlug != null && !providedSlug.trim().isEmpty()) {
            return providedSlug.trim().toLowerCase().replaceAll("[^a-z0-9]+", "-");
        }
        return name.toLowerCase().replaceAll("[^a-z0-9]+", "-");
    }
}
