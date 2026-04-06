package com.asal.ecommerce.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class ImageUploadService {
    
    @Value("${app.upload.dir:uploads}")
    private String uploadDir;
    
    public String uploadCategoryImage(MultipartFile file) throws IOException {
        return uploadImage(file, "categories");
    }
    
    public String uploadSubcategoryImage(MultipartFile file) throws IOException {
        return uploadImage(file, "subcategories");
    }
    

public String uploadProductImage(MultipartFile file) throws IOException {
        return uploadImage(file, "products");
    }
 
    public String uploadProductHoverImage(MultipartFile file) throws IOException {
        return uploadImage(file, "products");
    }







    private String uploadImage(MultipartFile file, String category) throws IOException {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }
        
        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }
        
        // Validate file size (10MB max)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("File size must be less than 10MB");
        }
        
        // Create directory structure
        Path categoryDir = Paths.get(uploadDir, category);
        Files.createDirectories(categoryDir);
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }
        
        String filename = UUID.randomUUID().toString() + extension;
        Path filePath = categoryDir.resolve(filename);
        
        // Save file
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        
        // Return relative path for database storage
        return "/" + uploadDir + "/" + category + "/" + filename;
    }
    
    public void deleteImage(String imagePath) {
        try {
            if (imagePath != null && imagePath.startsWith("/")) {
                // Remove leading slash for file system path
                Path filePath = Paths.get(imagePath.substring(1));
                Files.deleteIfExists(filePath);
            }
        } catch (IOException e) {
            // Log error but don't throw - image deletion is not critical
            System.err.println("Failed to delete image: " + imagePath + " - " + e.getMessage());
        }
    }
}
