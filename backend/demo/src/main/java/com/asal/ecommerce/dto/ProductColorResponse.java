// ProductColorResponse.java
package com.asal.ecommerce.dto;

import lombok.Data;
import java.util.List;

@Data
public class ProductColorResponse {
    private Long id;
    private String colorName;
    private String colorHex;
    private int stock;
    private List<ProductColorImageResponse> images;
}