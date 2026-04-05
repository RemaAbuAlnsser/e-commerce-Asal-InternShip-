package com.asal.ecommerce.controller.customer;

import com.asal.ecommerce.dto.CategoryResponse;
import com.asal.ecommerce.dto.SubcategoryResponse;
import com.asal.ecommerce.service.CategoryService;
import com.asal.ecommerce.service.SubcategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController("customerCategoryController")
@RequestMapping("/api/categories")
public class CategoryController {
    
    @Autowired
    private CategoryService categoryService;
    
    @Autowired
    private SubcategoryService subcategoryService;
    
    @GetMapping
    public ResponseEntity<Page<CategoryResponse>> getActiveCategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        try {
            Page<CategoryResponse> categories = categoryService.getActiveCategories(page, size, sortBy, direction);
            return ResponseEntity.ok(categories);
        } catch (RuntimeException e) {
            throw e; // Let GlobalExceptionHandler handle it
        }
    }
    
    @GetMapping("/{slug}")
    public ResponseEntity<CategoryResponse> getActiveCategoryBySlug(@PathVariable String slug) {
        try {
            CategoryResponse response = categoryService.getActiveCategoryBySlug(slug);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            throw e; // Let GlobalExceptionHandler handle it
        }
    }
    
    @GetMapping("/{slug}/subcategories")
    public ResponseEntity<Page<SubcategoryResponse>> getActiveSubcategoriesByCategorySlug(
            @PathVariable String slug,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        try {
            Page<SubcategoryResponse> subcategories = subcategoryService.getActiveSubcategoriesByCategorySlug(slug, page, size, sortBy, direction);
            return ResponseEntity.ok(subcategories);
        } catch (RuntimeException e) {
            throw e; // Let GlobalExceptionHandler handle it
        }
    }
}
