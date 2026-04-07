// ProductColorImageResponse.java
package com.asal.ecommerce.dto;

import lombok.Data;

@Data
public class ProductColorImageResponse {
    private Long id;
    private String imageUrl;
    private int sortOrder;
}