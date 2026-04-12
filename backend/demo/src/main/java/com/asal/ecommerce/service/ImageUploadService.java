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

    // ── NEW: sub-images per color variant ────────────────────────────────────
    public String uploadColorImage(MultipartFile file) throws IOException {
        return uploadImage(file, "products/colors");
    }

    public String uploadSettingsImage(MultipartFile file) throws IOException {
        return uploadImage(file, "settings");
    }

    public String uploadSiteImage(MultipartFile file) throws IOException {
        return uploadImage(file, "site-images");
    }

    // ── shared private logic ──────────────────────────────────────────────────
    private String uploadImage(MultipartFile file, String category) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        // Validate file type
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("File must be an image");
        }

        // Validate file size (10 MB max)
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new IllegalArgumentException("File size must be less than 10MB");
        }

        // Create directory if it doesn't exist
        Path categoryDir = Paths.get(uploadDir, category);
        Files.createDirectories(categoryDir);

        // Generate unique filename keeping the original extension
        String originalFilename = file.getOriginalFilename();
        String extension = "";
        if (originalFilename != null && originalFilename.contains(".")) {
            extension = originalFilename.substring(originalFilename.lastIndexOf("."));
        }

        String filename  = UUID.randomUUID().toString() + extension;
        Path   filePath  = categoryDir.resolve(filename);

        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        // Return path stored in DB  →  /uploads/products/colors/uuid.jpg
        return "/" + uploadDir + "/" + category + "/" + filename;
    }

    public void deleteImage(String imagePath) {
        try {
            if (imagePath != null && imagePath.startsWith("/")) {
                Path filePath = Paths.get(imagePath.substring(1));
                Files.deleteIfExists(filePath);
            }
        } catch (IOException e) {
            System.err.println("Failed to delete image: " + imagePath + " - " + e.getMessage());
        }
    }
}