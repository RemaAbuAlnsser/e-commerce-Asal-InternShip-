package com.asal.ecommerce.controller;

import com.asal.ecommerce.dto.*;
import com.asal.ecommerce.service.SubcategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/admin/subcategories")
public class SubcategoryController {
    
    @Autowired
    private SubcategoryService subcategoryService;
    
    @PostMapping
    public ResponseEntity<SubcategoryResponse> createSubcategory(@Valid @RequestBody SubcategoryCreateRequest request) {
        try {
            SubcategoryResponse response = subcategoryService.createSubcategory(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (RuntimeException e) {
            throw e; // Let GlobalExceptionHandler handle it
        }
    }
    
    @PutMapping("/{id}")
    public ResponseEntity<SubcategoryResponse> updateSubcategory(
            @PathVariable Long id, 
            @Valid @RequestBody SubcategoryUpdateRequest request) {
        try {
            SubcategoryResponse response = subcategoryService.updateSubcategory(id, request);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            throw e; // Let GlobalExceptionHandler handle it
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<SubcategoryResponse> getSubcategoryById(@PathVariable Long id) {
        try {
            SubcategoryResponse response = subcategoryService.getSubcategoryById(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            throw e; // Let GlobalExceptionHandler handle it
        }
    }
    
    @GetMapping
    public ResponseEntity<Page<SubcategoryResponse>> getAllSubcategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        try {
            Page<SubcategoryResponse> subcategories = subcategoryService.getAllSubcategories(page, size, sortBy, direction);
            return ResponseEntity.ok(subcategories);
        } catch (RuntimeException e) {
            throw e; // Let GlobalExceptionHandler handle it
        }
    }
    
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<Page<SubcategoryResponse>> getSubcategoriesByCategory(
            @PathVariable Long categoryId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        try {
            Page<SubcategoryResponse> subcategories = subcategoryService.getSubcategoriesByCategory(categoryId, page, size, sortBy, direction);
            return ResponseEntity.ok(subcategories);
        } catch (RuntimeException e) {
            throw e; // Let GlobalExceptionHandler handle it
        }
    }
    
    @GetMapping("/search")
    public ResponseEntity<Page<SubcategoryResponse>> searchSubcategories(
            @RequestParam String name,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        try {
            Page<SubcategoryResponse> subcategories = subcategoryService.searchSubcategories(name, page, size, sortBy, direction);
            return ResponseEntity.ok(subcategories);
        } catch (RuntimeException e) {
            throw e; // Let GlobalExceptionHandler handle it
        }
    }
    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSubcategory(@PathVariable Long id) {
        try {
            subcategoryService.deleteSubcategory(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            throw e; // Let GlobalExceptionHandler handle it
        }
    }
    
    @PatchMapping("/{id}/status")
    public ResponseEntity<SubcategoryResponse> updateSubcategoryStatus(
            @PathVariable Long id,
            @RequestParam Boolean isActive) {
        try {
            SubcategoryResponse response = subcategoryService.updateSubcategoryStatus(id, isActive);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            throw e; // Let GlobalExceptionHandler handle it
        }
    }
}
