package com.asal.ecommerce.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
public class ProductResponse {

    private Long id;
    private String name;
    private String sku;
    private String description;
    private BigDecimal price;
    private BigDecimal oldPrice;
    private Integer stock;
    private String status;
    private Boolean isFeatured;
    private Boolean isExclusive;

    // ── Nested refs (flat IDs + names, no deep nesting) ──────────────────────

    private Long categoryId;
    private String categoryName;

    private Long subcategoryId;
    private String subcategoryName;

    private Long brandId;
    private String brandName;

    // ── Images ───────────────────────────────────────────────────────────────

    private String imageUrl;
    private String hoverImageUrl;

    // ── Audit ────────────────────────────────────────────────────────────────

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}