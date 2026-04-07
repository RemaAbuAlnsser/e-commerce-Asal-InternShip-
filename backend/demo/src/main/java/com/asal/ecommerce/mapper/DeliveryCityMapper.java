package com.asal.ecommerce.mapper;

import com.asal.ecommerce.dto.DeliveryCityRequest;
import com.asal.ecommerce.dto.DeliveryCityResponse;
import com.asal.ecommerce.model.DeliveryCity;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface DeliveryCityMapper {

    // =============================
    // Request → Entity
    // =============================
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    DeliveryCity toEntity(DeliveryCityRequest request);

    // =============================
    // Entity → Response
    // =============================
    DeliveryCityResponse toResponse(DeliveryCity entity);

    // =============================
    // Update existing entity
    // =============================
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    void updateEntityFromRequest(DeliveryCityRequest request, @MappingTarget DeliveryCity entity);
}