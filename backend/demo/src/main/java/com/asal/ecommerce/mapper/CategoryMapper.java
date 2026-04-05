package com.asal.ecommerce.mapper;

import com.asal.ecommerce.dto.*;
import com.asal.ecommerce.model.Category;
import com.asal.ecommerce.model.Subcategory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class CategoryMapper {
    
    public Category toEntity(CategoryCreateRequest request) {
        Category category = new Category();
        category.setName(request.getName().trim());
        category.setSlug(generateSlug(request.getSlug(), request.getName().trim()));
        category.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        category.setImageUrl(request.getImageUrl());
        category.setIsActive(true);
        return category;
    }
    
    public CategoryResponse toResponse(Category category) {
        CategoryResponse response = new CategoryResponse();
        response.setId(category.getId());
        response.setName(category.getName());
        response.setSlug(category.getSlug());
        response.setDescription(category.getDescription());
        response.setImageUrl(category.getImageUrl());
        response.setIsActive(category.getIsActive());
        response.setCreatedAt(category.getCreatedAt());
        response.setUpdatedAt(category.getUpdatedAt());
        
        // Map subcategories if loaded
        if (category.getSubcategories() != null) {
            List<SubcategoryResponse> subcategoryResponses = category.getSubcategories().stream()
                .map(this::mapSubcategoryToResponse)
                .collect(Collectors.toList());
            response.setSubcategories(subcategoryResponses);
        }
        
        return response;
    }
    
    public void updateEntity(Category category, CategoryUpdateRequest request) {
        category.setName(request.getName().trim());
        category.setSlug(generateSlug(request.getSlug(), request.getName().trim()));
        category.setDescription(request.getDescription() != null ? request.getDescription().trim() : null);
        category.setImageUrl(request.getImageUrl());
    }
    
    private SubcategoryResponse mapSubcategoryToResponse(Subcategory subcategory) {
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
    
    public String generateSlug(String providedSlug, String name) {
        if (providedSlug != null && !providedSlug.trim().isEmpty()) {
            return providedSlug.trim().toLowerCase().replaceAll("[^a-z0-9]+", "-");
        }
        return name.toLowerCase().replaceAll("[^a-z0-9]+", "-");
    }
}
