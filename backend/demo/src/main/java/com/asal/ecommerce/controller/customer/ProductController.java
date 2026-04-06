package com.asal.ecommerce.controller.customer;

import com.asal.ecommerce.dto.ProductResponse;
import com.asal.ecommerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController("customerProductController")
@RequestMapping("/api/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // GET /api/products/{id}
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getById(id));
    }

    // GET /api/products/sku/{sku}
    @GetMapping("/sku/{sku}")
    public ResponseEntity<ProductResponse> getBySku(@PathVariable String sku) {
        return ResponseEntity.ok(productService.getBySku(sku));
    }

    // GET /api/products?categoryId=&subcategoryId=&brandId=&isFeatured=&isExclusive=
    // Always filters to status=active for public customers
    @GetMapping
    public ResponseEntity<Page<ProductResponse>> getAll(
            @RequestParam(required = false) Long    categoryId,
            @RequestParam(required = false) Long    subcategoryId,
            @RequestParam(required = false) Long    brandId,
            @RequestParam(required = false) Boolean isFeatured,
            @RequestParam(required = false) Boolean isExclusive,
            @PageableDefault(size = 20, sort = "id") Pageable pageable
    ) {
        return ResponseEntity.ok(
                productService.getAll(categoryId, subcategoryId, brandId, "active", isFeatured, isExclusive, pageable)
        );
    }

    // GET /api/products/search?keyword=
    @GetMapping("/search")
    public ResponseEntity<Page<ProductResponse>> search(
            @RequestParam String keyword,
            @PageableDefault(size = 20) Pageable pageable
    ) {
        return ResponseEntity.ok(productService.search(keyword, pageable));
    }
}