// ProductResponse.java
package com.asal.ecommerce.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ProductResponse {
    private Long id;
    private String name;
    private String sku;
    private String description;
    private BigDecimal price;
    private BigDecimal oldPrice;
    private String status;
    private boolean featured;
    private boolean exclusive;
    private Long categoryId;
    private String categoryName;
    private Long subcategoryId;
    private String subcategoryName;
    private Long brandId;
    private String brandName;
    private String imageUrl;
    private String hoverImageUrl;
    private int totalStock;           // computed — sum of color stocks
    private List<ProductColorResponse> colors;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}