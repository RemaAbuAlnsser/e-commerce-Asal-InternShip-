package com.asal.ecommerce.mapper;

import com.asal.ecommerce.dto.*;
import com.asal.ecommerce.model.Brand;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface BrandMapper {
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "isActive", constant = "true")
    @Mapping(target = "name", expression = "java(request.getName().trim())")
    Brand toEntity(BrandCreateRequest request);
    
    BrandResponse toResponse(Brand brand);
    
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "name", expression = "java(request.getName().trim())")
    @Mapping(target = "isActive", source = "isActive")
    void updateEntity(@MappingTarget Brand brand, BrandUpdateRequest request);
}
