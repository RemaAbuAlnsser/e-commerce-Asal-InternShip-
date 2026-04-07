// ProductColorImageRequest.java
package com.asal.ecommerce.dto;

import lombok.Data;

@Data
public class ProductColorImageRequest {
    private int sortOrder;
    // imageUrl is set after upload — not sent by client directly
}