package com.asal.ecommerce.controller.admin;

import com.asal.ecommerce.dto.*;
import com.asal.ecommerce.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController("adminCategoryController")
@RequestMapping("/api/admin/categories")
public class CategoryController {
    
    @Autowired
    private CategoryService categoryService;
    
    @PostMapping
    public ResponseEntity<CategoryResponse> createCategory(@Valid @RequestBody CategoryCreateRequest request) {
        try {
            CategoryResponse response = categoryService.createCategory(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            throw e; // Let GlobalExceptionHandler handle it
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<CategoryResponse> updateCategory(
            @PathVariable Long id, 
            @Valid @RequestBody CategoryUpdateRequest request) {
        try {
            CategoryResponse response = categoryService.updateCategory(id, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            throw e; // Let GlobalExceptionHandler handle it
        }
    }
    
    @GetMapping
    public ResponseEntity<Page<CategoryResponse>> getAllCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        try {
            Page<CategoryResponse> categories = categoryService.getAllCategories(page, size, sortBy, direction);
            return ResponseEntity.ok(categories);
        } catch (RuntimeException e) {
            throw e; // Let GlobalExceptionHandler handle it
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<CategoryResponse> getCategoryById(@PathVariable Long id) {
        try {
            CategoryResponse response = categoryService.getCategoryById(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            throw e; // Let GlobalExceptionHandler handle it
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<Page<CategoryResponse>> searchCategories(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        try {
            Page<CategoryResponse> categories = categoryService.searchCategories(name, page, size, sortBy, direction);
            return ResponseEntity.ok(categories);
        } catch (RuntimeException e) {
            throw e; // Let GlobalExceptionHandler handle it
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCategory(@PathVariable Long id) {
        try {
            categoryService.deleteCategory(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            throw e; // Let GlobalExceptionHandler handle it
        }
    }
    
    @PatchMapping("/{id}/status")
    public ResponseEntity<CategoryResponse> updateCategoryStatus(
            @PathVariable Long id,
            @RequestParam Boolean isActive) {
        try {
            CategoryResponse response = categoryService.updateCategoryStatus(id, isActive);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            throw e; // Let GlobalExceptionHandler handle it
        }
    }
}
