package com.asal.ecommerce.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class ProductUpdateRequest {

    @NotBlank(message = "Product name is required")
    @Size(max = 191, message = "Name must not exceed 191 characters")
    private String name;

    @NotBlank(message = "SKU is required")
    @Size(max = 100, message = "SKU must not exceed 100 characters")
    private String sku;

    private String description;

    @NotNull(message = "Price is required")
    @DecimalMin(value = "0.00", message = "Price must be zero or positive")
    @Digits(integer = 8, fraction = 2, message = "Invalid price format")
    private BigDecimal price;

    @DecimalMin(value = "0.00", message = "Old price must be zero or positive")
    @Digits(integer = 8, fraction = 2, message = "Invalid old price format")
    private BigDecimal oldPrice;

    @NotNull(message = "Stock is required")
    @Min(value = 0, message = "Stock cannot be negative")
    private Integer stock;

    @NotBlank(message = "Status is required")
    private String status;

    private Boolean isFeatured;

    private Boolean isExclusive;

    @NotNull(message = "Category ID is required")
    private Long categoryId;

    private Long subcategoryId;

    private Long brandId;

    @Size(max = 500, message = "Image URL must not exceed 500 characters")
    private String imageUrl;

    @Size(max = 500, message = "Hover image URL must not exceed 500 characters")
    private String hoverImageUrl;
}