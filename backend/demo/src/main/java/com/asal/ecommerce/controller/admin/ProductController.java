package com.asal.ecommerce.controller.admin;

import com.asal.ecommerce.dto.ProductCreateRequest;
import com.asal.ecommerce.dto.ProductUpdateRequest;
import com.asal.ecommerce.dto.ProductResponse;
import com.asal.ecommerce.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/admin/products")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/admin/products
    // Content-Type: multipart/form-data
    // ─────────────────────────────────────────────────────────────────────────
    @PostMapping(consumes = "multipart/form-data")
    public ResponseEntity<ProductResponse> createProduct(

            // ── basic fields ─────────────────────────────────────────────────
            @RequestParam("name")                                           String        name,
            @RequestParam(value = "description",   required = false)       String        description,
            @RequestParam("price")                                          BigDecimal    price,
            @RequestParam(value = "oldPrice",      required = false)       BigDecimal    oldPrice,
            @RequestParam(value = "status",        defaultValue = "active") String       status,
            @RequestParam(value = "featured",      defaultValue = "false") boolean       featured,
            @RequestParam(value = "exclusive",     defaultValue = "false") boolean       exclusive,
            @RequestParam("categoryId")                                     Long          categoryId,
            @RequestParam(value = "subcategoryId", required = false)       Long          subcategoryId,
            @RequestParam(value = "brandId",       required = false)       Long          brandId,

            // ── landing-page images ───────────────────────────────────────────
            @RequestPart(value = "primeImage",     required = false) MultipartFile primeImage,
            @RequestPart(value = "hoverImage",     required = false) MultipartFile hoverImage,

            // ── color variant metadata (parallel arrays, same index = same color)
            @RequestParam(value = "colorNames",    required = false) List<String>  colorNames,
            @RequestParam(value = "colorHexes",    required = false) List<String>  colorHexes,
            @RequestParam(value = "colorStocks",   required = false) List<Integer> colorStocks,

            // ── sub-images per color (colorImages[0] → color at index 0) ─────
            @RequestPart(value = "colorImages[0]", required = false) List<MultipartFile> colorImages0,
            @RequestPart(value = "colorImages[1]", required = false) List<MultipartFile> colorImages1,
            @RequestPart(value = "colorImages[2]", required = false) List<MultipartFile> colorImages2,
            @RequestPart(value = "colorImages[3]", required = false) List<MultipartFile> colorImages3,
            @RequestPart(value = "colorImages[4]", required = false) List<MultipartFile> colorImages4

    ) throws IOException {

        ProductCreateRequest req = new ProductCreateRequest();
        req.setName(name);
        req.setDescription(description);
        req.setPrice(price);
        req.setOldPrice(oldPrice);
        req.setStatus(status);
        req.setFeatured(featured);
        req.setExclusive(exclusive);
        req.setCategoryId(categoryId);
        req.setSubcategoryId(subcategoryId);
        req.setBrandId(brandId);

        // Collect colorImages into an ordered list
        List<List<MultipartFile>> colorImages = Arrays.asList(
                colorImages0 != null ? colorImages0 : Collections.emptyList(),
                colorImages1 != null ? colorImages1 : Collections.emptyList(),
                colorImages2 != null ? colorImages2 : Collections.emptyList(),
                colorImages3 != null ? colorImages3 : Collections.emptyList(),
                colorImages4 != null ? colorImages4 : Collections.emptyList()
        );

        return ResponseEntity.ok(
                productService.createProduct(req, primeImage, hoverImage,
                        colorNames, colorHexes, colorStocks, colorImages)
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/admin/products
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping
    public ResponseEntity<List<ProductResponse>> getAllProducts() {
        return ResponseEntity.ok(productService.getAllProducts());
    }

    // ─────────────────────────────────────────────────────────────────────────
    // GET /api/admin/products/{id}
    // ─────────────────────────────────────────────────────────────────────────
    @GetMapping("/{id}")
    public ResponseEntity<ProductResponse> getProduct(@PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PUT /api/admin/products/{id}
    // Content-Type: multipart/form-data
    // ─────────────────────────────────────────────────────────────────────────
    @PutMapping(value = "/{id}", consumes = "multipart/form-data")
    public ResponseEntity<ProductResponse> updateProduct(
            @PathVariable Long id,
            @RequestParam(value = "name",          required = false) String     name,
            @RequestParam(value = "description",   required = false) String     description,
            @RequestParam(value = "price",         required = false) BigDecimal price,
            @RequestParam(value = "oldPrice",      required = false) BigDecimal oldPrice,
            @RequestParam(value = "status",        required = false) String     status,
            @RequestParam(value = "featured",      defaultValue = "false") boolean featured,
            @RequestParam(value = "exclusive",     defaultValue = "false") boolean exclusive,
            @RequestParam(value = "categoryId",    required = false) Long       categoryId,
            @RequestParam(value = "subcategoryId", required = false) Long       subcategoryId,
            @RequestParam(value = "brandId",       required = false) Long       brandId,
            @RequestPart(value = "primeImage",     required = false) MultipartFile primeImage,
            @RequestPart(value = "hoverImage",     required = false) MultipartFile hoverImage
    ) throws IOException {

        ProductUpdateRequest req = new ProductUpdateRequest();
        req.setName(name);
        req.setDescription(description);
        req.setPrice(price);
        req.setOldPrice(oldPrice);
        req.setStatus(status);
        req.setFeatured(featured);
        req.setExclusive(exclusive);
        req.setCategoryId(categoryId);
        req.setSubcategoryId(subcategoryId);
        req.setBrandId(brandId);

        return ResponseEntity.ok(productService.updateProduct(id, req, primeImage, hoverImage));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // POST /api/admin/products/{id}/colors
    // Add a new color variant to an existing product
    // ─────────────────────────────────────────────────────────────────────────
    @PostMapping(value = "/{id}/colors", consumes = "multipart/form-data")
    public ResponseEntity<ProductResponse> addColor(
            @PathVariable Long id,
            @RequestParam("colorName")                              String             colorName,
            @RequestParam("colorHex")                               String             colorHex,
            @RequestParam("stock")                                  int                stock,
            @RequestPart(value = "images", required = false) List<MultipartFile> images
    ) throws IOException {
        return ResponseEntity.ok(
                productService.addColorToProduct(id, colorName, colorHex, stock, images)
        );
    }

    // ─────────────────────────────────────────────────────────────────────────
    // PATCH /api/admin/products/{id}/colors/{colorId}/stock
    // Update stock of one specific color
    // ─────────────────────────────────────────────────────────────────────────
    @PatchMapping("/{id}/colors/{colorId}/stock")
    public ResponseEntity<ProductResponse> updateColorStock(
            @PathVariable Long id,
            @PathVariable Long colorId,
            @RequestParam("stock") int stock
    ) {
        return ResponseEntity.ok(productService.updateColorStock(id, colorId, stock));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/admin/products/{id}/colors/{colorId}
    // Remove one color variant (and all its sub-images)
    // ─────────────────────────────────────────────────────────────────────────
    @DeleteMapping("/{id}/colors/{colorId}")
    public ResponseEntity<ProductResponse> deleteColor(
            @PathVariable Long id,
            @PathVariable Long colorId
    ) {
        return ResponseEntity.ok(productService.deleteColor(id, colorId));
    }

    // ─────────────────────────────────────────────────────────────────────────
    // DELETE /api/admin/products/{id}
    // Delete entire product with all colors and images
    // ─────────────────────────────────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }
}