package com.asal.ecommerce.mapper;

import com.asal.ecommerce.dto.OrderItemResponse;
import com.asal.ecommerce.dto.OrderRequest;
import com.asal.ecommerce.dto.OrderResponse;
import com.asal.ecommerce.model.Order;
import com.asal.ecommerce.model.OrderItem;
import com.asal.ecommerce.model.User;
import org.mapstruct.*;

@Mapper(componentModel = "spring")
public interface OrderMapper {

    // ===============================
    // Request → Entity  (items built separately in service)
    // ===============================
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "user", source = "user")
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "subtotal", ignore = true)
    @Mapping(target = "total", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    @Mapping(target = "customerEmail", source = "request.customerEmail")
    Order toEntity(OrderRequest request, User user);

    // ===============================
    // Entity → Response
    // ===============================
    @Mapping(target = "userId", source = "user.id")
    @Mapping(target = "items", ignore = true)
    OrderResponse toResponse(Order order);

    // ===============================
    // OrderItem → OrderItemResponse
    // ===============================
    @Mapping(target = "productId", source = "product.id")
    @Mapping(target = "productColorId", source = "productColor.id")
    OrderItemResponse toItemResponse(OrderItem item);

    // ===============================
    // Update existing entity
    // ===============================
    @BeanMapping(nullValuePropertyMappingStrategy = NullValuePropertyMappingStrategy.IGNORE)
    @Mapping(target = "user", source = "user")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "items", ignore = true)
    @Mapping(target = "subtotal", ignore = true)
    @Mapping(target = "total", ignore = true)
    @Mapping(target = "createdAt", ignore = true)
    @Mapping(target = "updatedAt", ignore = true)
    void updateEntity(@MappingTarget Order order, OrderRequest request, User user);
}
