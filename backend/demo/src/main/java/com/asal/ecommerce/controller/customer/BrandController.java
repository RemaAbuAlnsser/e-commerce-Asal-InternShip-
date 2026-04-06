package com.asal.ecommerce.controller.customer;

import com.asal.ecommerce.dto.BrandResponse;
import com.asal.ecommerce.service.BrandService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController("customerBrandController")
@RequestMapping("/api/brands")
public class BrandController {
    
    @Autowired
    private BrandService brandService;
    
    @GetMapping
    public ResponseEntity<Page<BrandResponse>> getActiveBrands(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "name") String sortBy,
            @RequestParam(defaultValue = "asc") String direction) {
        try {
            Page<BrandResponse> brands = brandService.getActiveBrands(page, size, sortBy, direction);
            return ResponseEntity.ok(brands);
        } catch (RuntimeException e) {
            throw e;
        }
    }
    
    @GetMapping("/{id}")
    public ResponseEntity<BrandResponse> getActiveBrandById(@PathVariable Long id) {
        try {
            BrandResponse response = brandService.getActiveBrandById(id);
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            throw e;
        }
    }
}
