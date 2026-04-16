package com.asal.ecommerce.dto;

import com.asal.ecommerce.model.CartItem;
import lombok.Data;

@Data
public class CartItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String productImage;
    private String categoryName;
    private double price;
    private Double oldPrice;
    private Integer colorId;
    private String colorName;
    private String colorHex;
    private int quantity;
    private int maxStock;

    public static CartItemResponse from(CartItem item) {
        CartItemResponse r = new CartItemResponse();
        r.id           = item.getId();
        r.productId    = item.getProductId();
        r.productName  = item.getProductName();
        r.productImage = item.getProductImage();
        r.categoryName = item.getCategoryName();
        r.price        = item.getPrice();
        r.oldPrice     = item.getOldPrice();
        r.colorId      = item.getColorId();
        r.colorName    = item.getColorName();
        r.colorHex     = item.getColorHex();
        r.quantity     = item.getQuantity();
        r.maxStock     = item.getMaxStock();
        return r;
    }
}
