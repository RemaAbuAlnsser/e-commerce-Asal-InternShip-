package com.asal.ecommerce.dto;

import com.asal.ecommerce.model.WishlistItem;
import lombok.Data;

@Data
public class WishlistItemResponse {
    private Long id;
    private Long productId;
    private String productName;
    private String productImage;
    private String categoryName;
    private double price;
    private Double oldPrice;
    private int totalStock;

    public static WishlistItemResponse from(WishlistItem item) {
        WishlistItemResponse r = new WishlistItemResponse();
        r.id           = item.getId();
        r.productId    = item.getProductId();
        r.productName  = item.getProductName();
        r.productImage = item.getProductImage();
        r.categoryName = item.getCategoryName();
        r.price        = item.getPrice();
        r.oldPrice     = item.getOldPrice();
        r.totalStock   = item.getTotalStock();
        return r;
    }
}
