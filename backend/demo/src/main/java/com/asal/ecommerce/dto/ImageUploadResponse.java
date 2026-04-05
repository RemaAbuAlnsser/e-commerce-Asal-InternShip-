package com.asal.ecommerce.dto;

import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ImageUploadResponse {
    private boolean success;
    private String message;
    private String imageUrl;
    
    public static ImageUploadResponse success(String imageUrl) {
        return new ImageUploadResponse(true, "Image uploaded successfully", imageUrl);
    }
    
    public static ImageUploadResponse error(String message) {
        return new ImageUploadResponse(false, message, null);
    }
}
