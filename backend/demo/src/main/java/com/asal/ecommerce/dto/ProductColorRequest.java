// ProductColorRequest.java
package com.asal.ecommerce.dto;

import lombok.Data;

@Data
public class ProductColorRequest {
    private String colorName;   // e.g. "Black"
    private String colorHex;    // e.g. "#1a1a1a"
    private int stock;
}