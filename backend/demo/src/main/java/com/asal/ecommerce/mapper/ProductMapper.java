package com.asal.ecommerce.mapper;

import com.asal.ecommerce.dto.ProductCreateRequest;
import com.asal.ecommerce.dto.ProductResponse;
import com.asal.ecommerce.dto.ProductUpdateRequest;
import com.asal.ecommerce.model.Product;
import org.mapstruct.*;

@Mapper(componentModel = "spring", nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
public interface ProductMapper {

    // ── Create ───────────────────────────────────────────────────────────────

    @Mapping(target = "id",          ignore = true)
    @Mapping(target = "category",    ignore = true)   // resolved in service
    @Mapping(target = "subcategory", ignore = true)   // resolved in service
    @Mapping(target = "brand",       ignore = true)   // resolved in service
    @Mapping(target = "createdAt",   ignore = true)
    @Mapping(target = "updatedAt",   ignore = true)
    Product toEntity(ProductCreateRequest request);

    // ── Update (merge into existing entity) ──────────────────────────────────

    @Mapping(target = "id",          ignore = true)
    @Mapping(target = "category",    ignore = true)
    @Mapping(target = "subcategory", ignore = true)
    @Mapping(target = "brand",       ignore = true)
    @Mapping(target = "createdAt",   ignore = true)
    @Mapping(target = "updatedAt",   ignore = true)
    void updateEntity(ProductUpdateRequest request, @MappingTarget Product product);

    // ── Response ─────────────────────────────────────────────────────────────

    @Mapping(target = "categoryId",      source = "category.id")
    @Mapping(target = "categoryName",    source = "category.name")
    @Mapping(target = "subcategoryId",   source = "subcategory.id")
    @Mapping(target = "subcategoryName", source = "subcategory.name")
    @Mapping(target = "brandId",         source = "brand.id")
    @Mapping(target = "brandName",       source = "brand.name")
    ProductResponse toResponse(Product product);
}