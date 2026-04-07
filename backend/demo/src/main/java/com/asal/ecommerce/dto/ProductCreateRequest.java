// ProductCreateRequest.java
package com.asal.ecommerce.dto;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class ProductCreateRequest {
    private String name;
    private String description;
    private BigDecimal price;
    private BigDecimal oldPrice;
    private String status = "active";
    private boolean featured;
    private boolean exclusive;
    private Long categoryId;
    private Long subcategoryId;
    private Long brandId;
}