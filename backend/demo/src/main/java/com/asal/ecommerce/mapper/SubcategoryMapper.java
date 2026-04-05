package com.asal.ecommerce.mapper;

import com.asal.ecommerce.dto.*;
import com.asal.ecommerce.model.Category;
import com.asal.ecommerce.model.Subcategory;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface SubcategoryMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "name", expression = "java(request.getName().trim())")
    @Mapping(target = "slug", expression = "java(generateSlug(request.getSlug(), request.getName().trim()))")
    @Mapping(target = "description", expression = "java(request.getDescription() != null ? request.getDescription().trim() : null)")
    @Mapping(target = "imageUrl", source = "request.imageUrl")
    @Mapping(target = "category", source = "category")
    Subcategory toEntity(SubcategoryCreateRequest request, Category category);
    
    @Mapping(target = "categoryId", source = "category.id")
    @Mapping(target = "categoryName", source = "category.name")
    SubcategoryResponse toResponse(Subcategory subcategory);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "name", expression = "java(request.getName().trim())")
    @Mapping(target = "slug", expression = "java(generateSlug(request.getSlug(), request.getName().trim()))")
    @Mapping(target = "description", expression = "java(request.getDescription() != null ? request.getDescription().trim() : null)")
    @Mapping(target = "imageUrl", source = "request.imageUrl")
    @Mapping(target = "category", source = "category")
    void updateEntity(@MappingTarget Subcategory subcategory, SubcategoryUpdateRequest request, Category category);
    
    default String generateSlug(String providedSlug, String name) {
        if (providedSlug != null && !providedSlug.trim().isEmpty()) {
            return providedSlug.trim().toLowerCase().replaceAll("[^a-z0-9]+", "-");
        }
        return name.toLowerCase().replaceAll("[^a-z0-9]+", "-");
    }
}
