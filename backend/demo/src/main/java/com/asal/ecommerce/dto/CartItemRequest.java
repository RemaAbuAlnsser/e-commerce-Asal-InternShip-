package com.asal.ecommerce.dto;

import lombok.Data;

@Data
public class CartItemRequest {
    private Long productId;
    private String productName;
    private String productImage;
    private String categoryName;
    private double price;
    private Double oldPrice;
    private Integer colorId;
    private String colorName;
    private String colorHex;
    private int quantity = 1;
    private int maxStock;
}
