package com.asal.ecommerce.controller.admin;

import com.asal.ecommerce.dto.ProductCreateRequest;
import com.asal.ecommerce.dto.ProductResponse;
import com.asal.ecommerce.dto.ProductUpdateRequest;
import com.asal.ecommerce.service.ImageUploadService;
import com.asal.ecommerce.service.ProductService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@RestController("adminProductController")
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService      productService;
    private final ImageUploadService  imageUploadService;

    // ── CRUD ─────────────────────────────────────────────────────────────────

    @PostMapping
    public ResponseEntity<ProductResponse> create(@Valid @RequestBody ProductCreateRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(productService.create(request));
    }

    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getAll(
            @RequestParam(required = false) Long    categoryId,
            @RequestParam(required = false) Long    subcategoryId,
            @RequestParam(required = false) Long    brandId,
            @RequestParam(required = false) String  status,
            @RequestParam(required = false) Boolean isFeatured,
            @RequestParam(required = false) Boolean isExclusive,
            @PageableDefault(size = 20, sort = "id") Pageable pageable
    ) {
        return ResponseEntity.ok(
                productService.getAll(categoryId, subcategoryId, brandId, status, isFeatured, isExclusive, pageable)
        );
    }

    @GetMapping("/search")
    public ResponseEntity<Page<ProductResponse>> search(
            @RequestParam String keyword,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(productService.search(keyword, pageable));
    }

    @GetMapping("/count")
    public ResponseEntity<Long> getProductCount() {
        try {
            Long count = productService.getProductCount();
            return ResponseEntity.ok(count);
        } catch (RuntimeException e) {
            throw e; // Let GlobalExceptionHandler handle it
        }
    }

    // ── Image Upload ──────────────────────────────────────────────────────────

    /**
     * Upload main product image.
     * POST /api/admin/products/{id}/image
     * Content-Type: multipart/form-data  |  field: "image"
     */
    @PostMapping(value = "/{id}/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadImage(
            @PathVariable Long id,
            @RequestParam("image") MultipartFile file
    ) throws IOException {
        String imageUrl = imageUploadService.uploadProductImage(file);
        ProductResponse updated = productService.updateImageUrl(id, imageUrl);

        Map<String, String> body = new HashMap<>();
        body.put("imageUrl", updated.getImageUrl());
        return ResponseEntity.ok(body);
    }

    /**
     * Upload hover product image.
     * POST /api/admin/products/{id}/hover-image
     * Content-Type: multipart/form-data  |  field: "image"
     */
    @PostMapping(value = "/{id}/hover-image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Map<String, String>> uploadHoverImage(
            @PathVariable Long id,
            @RequestParam("image") MultipartFile file
    ) throws IOException {
        String hoverImageUrl = imageUploadService.uploadProductHoverImage(file);
        ProductResponse updated = productService.updateHoverImageUrl(id, hoverImageUrl);

        Map<String, String> body = new HashMap<>();
        body.put("hoverImageUrl", updated.getHoverImageUrl());
        return ResponseEntity.ok(body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getById(id));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductResponse> update(
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest request
    ) {
        return ResponseEntity.ok(productService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        productService.delete(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Delete main product image.
     * DELETE /api/admin/products/{id}/image
     */
    @DeleteMapping("/{id}/image")
    public ResponseEntity<Void> deleteImage(@PathVariable Long id) {
        productService.deleteImageUrl(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * Delete hover product image.
     * DELETE /api/admin/products/{id}/hover-image
     */
    @DeleteMapping("/{id}/hover-image")
    public ResponseEntity<Void> deleteHoverImage(@PathVariable Long id) {
        productService.deleteHoverImageUrl(id);
        return ResponseEntity.noContent().build();
    }
}