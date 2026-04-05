package com.asal.ecommerce.controller.customer;

import com.asal.ecommerce.dto.SubcategoryResponse;
import com.asal.ecommerce.service.SubcategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController("customerSubcategoryController")
@RequestMapping("/api/subcategories")
public class SubcategoryController {
    
    @Autowired
    private SubcategoryService subcategoryService;
    
    @GetMapping
    public ResponseEntity<Page<SubcategoryResponse>> getActiveSubcategories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        try {
            Page<SubcategoryResponse> subcategories = subcategoryService.getActiveSubcategories(page, size, sortBy, direction);
            return ResponseEntity.ok(subcategories);
        } catch (RuntimeException e) {
            throw e; // Let GlobalExceptionHandler handle it
        }
    }
    
    @GetMapping("/{slug}")
    public ResponseEntity<SubcategoryResponse> getActiveSubcategoryBySlug(@PathVariable String slug) {
        try {
            SubcategoryResponse response = subcategoryService.getActiveSubcategoryBySlug(slug);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            throw e; // Let GlobalExceptionHandler handle it
        }
    }
}
