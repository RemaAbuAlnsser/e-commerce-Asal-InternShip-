package com.asal.ecommerce.controller.admin;

import com.asal.ecommerce.dto.*;
import com.asal.ecommerce.service.BrandService;
import com.asal.ecommerce.service.ImageUploadService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import jakarta.validation.Valid;

@RestController("adminBrandController")
@RequestMapping("/api/admin/brands")
public class BrandController {
    
    @Autowired
    private BrandService brandService;
    
    @Autowired
    private ImageUploadService imageUploadService;
    
    @PostMapping
    public ResponseEntity<BrandResponse> createBrand(@Valid @RequestBody BrandCreateRequest request) {
        try {
            BrandResponse response = brandService.createBrand(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            throw e;
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<BrandResponse> updateBrand(
            @PathVariable Long id, 
            @Valid @RequestBody BrandUpdateRequest request) {
        try {
            BrandResponse response = brandService.updateBrand(id, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            throw e;
        }
    }
    
    @GetMapping
    public ResponseEntity<Page<BrandResponse>> getAllBrands(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        try {
            Page<BrandResponse> brands = brandService.getAllBrands(page, size, sortBy, direction);
            return ResponseEntity.ok(brands);
        } catch (RuntimeException e) {
            throw e;
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<BrandResponse> getBrandById(@PathVariable Long id) {
        try {
            BrandResponse response = brandService.getBrandById(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            throw e;
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<Page<BrandResponse>> searchBrands(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        try {
            Page<BrandResponse> brands = brandService.searchBrands(name, page, size, sortBy, direction);
            return ResponseEntity.ok(brands);
        } catch (RuntimeException e) {
            throw e;
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteBrand(@PathVariable Long id) {
        try {
            brandService.deleteBrand(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            throw e;
        }
    }
    
    @PatchMapping("/{id}/status")
    public ResponseEntity<BrandResponse> updateBrandStatus(
            @PathVariable Long id,
            @RequestParam Boolean isActive) {
        try {
            BrandResponse response = brandService.updateBrandStatus(id, isActive);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            throw e;
        }
    }
    
    @GetMapping("/count")
    public ResponseEntity<Long> getBrandCount() {
        try {
            Long count = brandService.getBrandCount();
            return ResponseEntity.ok(count);
        } catch (RuntimeException e) {
            throw e;
        }
    }
    
    @PostMapping("/upload-logo")
    public ResponseEntity<ImageUploadResponse> uploadBrandLogo(@RequestParam("image") MultipartFile file) {
        try {
            String imageUrl = imageUploadService.uploadCategoryImage(file);
            return ResponseEntity.ok(ImageUploadResponse.success(imageUrl));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ImageUploadResponse.error(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ImageUploadResponse.error("Failed to upload logo"));
        }
    }
}
