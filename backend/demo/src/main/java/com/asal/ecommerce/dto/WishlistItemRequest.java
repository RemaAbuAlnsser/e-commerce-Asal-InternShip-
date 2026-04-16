package com.asal.ecommerce.dto;

import lombok.Data;

@Data
public class WishlistItemRequest {
    private Long productId;
    private String productName;
    private String productImage;
    private String categoryName;
    private double price;
    private Double oldPrice;
    private int totalStock;
}
