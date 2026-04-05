package com.asal.ecommerce.mapper;

import com.asal.ecommerce.dto.*;
import com.asal.ecommerce.model.Category;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface CategoryMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "subcategories", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "name", expression = "java(request.getName().trim())")
    @Mapping(target = "slug", expression = "java(generateSlug(request.getSlug(), request.getName().trim()))")
    @Mapping(target = "description", expression = "java(request.getDescription() != null ? request.getDescription().trim() : null)")
    Category toEntity(CategoryCreateRequest request);
    
    CategoryResponse toResponse(Category category);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "subcategories", ignore = true)
    @Mapping(target = "name", expression = "java(request.getName().trim())")
    @Mapping(target = "slug", expression = "java(generateSlug(request.getSlug(), request.getName().trim()))")
    @Mapping(target = "description", expression = "java(request.getDescription() != null ? request.getDescription().trim() : null)")
    void updateEntity(@MappingTarget Category category, CategoryUpdateRequest request);
    
    default String generateSlug(String providedSlug, String name) {
        if (providedSlug != null && !providedSlug.trim().isEmpty()) {
            return providedSlug.trim().toLowerCase().replaceAll("[^a-z0-9]+", "-");
        }
        return name.toLowerCase().replaceAll("[^a-z0-9]+", "-");
    }
}
