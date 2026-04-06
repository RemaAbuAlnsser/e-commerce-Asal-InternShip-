package com.asal.ecommerce.service;

import com.asal.ecommerce.dto.ProductCreateRequest;
import com.asal.ecommerce.dto.ProductResponse;
import com.asal.ecommerce.dto.ProductUpdateRequest;
import com.asal.ecommerce.mapper.ProductMapper;
import com.asal.ecommerce.model.Brand;
import com.asal.ecommerce.model.Category;
import com.asal.ecommerce.model.Product;
import com.asal.ecommerce.model.Subcategory;
import com.asal.ecommerce.repository.BrandRepository;
import com.asal.ecommerce.repository.CategoryRepository;
import com.asal.ecommerce.repository.ProductRepository;
import com.asal.ecommerce.repository.SubcategoryRepository;
import com.asal.ecommerce.service.ImageUploadService;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository     productRepository;
    private final CategoryRepository    categoryRepository;
    private final SubcategoryRepository subcategoryRepository;
    private final BrandRepository       brandRepository;
    private final ProductMapper         productMapper;
    private final ImageUploadService    imageUploadService;

    // ── Create ───────────────────────────────────────────────────────────────

    @Transactional
    public ProductResponse create(ProductCreateRequest request) {
        if (productRepository.existsBySku(request.getSku())) {
            throw new IllegalArgumentException("SKU already exists: " + request.getSku());
        }

        Product product = productMapper.toEntity(request);
        resolveRelations(product, request.getCategoryId(), request.getSubcategoryId(), request.getBrandId());

        return productMapper.toResponse(productRepository.save(product));
    }

    // ── Read ─────────────────────────────────────────────────────────────────

    @Transactional(readOnly = true)
    public ProductResponse getById(Long id) {
        return productMapper.toResponse(findOrThrow(id));
    }

    @Transactional(readOnly = true)
    public ProductResponse getBySku(String sku) {
        Product product = productRepository.findBySku(sku)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with SKU: " + sku));
        return productMapper.toResponse(product);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> getAll(
            Long    categoryId,
            Long    subcategoryId,
            Long    brandId,
            String  status,
            Boolean isFeatured,
            Boolean isExclusive,
            Pageable pageable
    ) {
        return productRepository
                .findAllFiltered(categoryId, subcategoryId, brandId, status, isFeatured, isExclusive, pageable)
                .map(productMapper::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<ProductResponse> search(String keyword, Pageable pageable) {
        return productRepository.searchByKeyword(keyword, pageable)
                .map(productMapper::toResponse);
    }

    // ── Update ───────────────────────────────────────────────────────────────

    @Transactional
    public ProductResponse update(Long id, ProductUpdateRequest request) {
        Product product = findOrThrow(id);

        if (productRepository.existsBySkuAndIdNot(request.getSku(), id)) {
            throw new IllegalArgumentException("SKU already exists: " + request.getSku());
        }

        productMapper.updateEntity(request, product);
        resolveRelations(product, request.getCategoryId(), request.getSubcategoryId(), request.getBrandId());

        return productMapper.toResponse(productRepository.save(product));
    }

    // ── Delete ───────────────────────────────────────────────────────────────

    @Transactional
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new EntityNotFoundException("Product not found with id: " + id);
        }
        productRepository.deleteById(id);
    }

    // ── Image management ─────────────────────────────────────────────────────

    @Transactional
    public ProductResponse updateImageUrl(Long id, String imageUrl) {
        Product product = findOrThrow(id);
        // Delete old file from disk before replacing
        if (product.getImageUrl() != null) {
            imageUploadService.deleteImage(product.getImageUrl());
        }
        product.setImageUrl(imageUrl);
        return productMapper.toResponse(productRepository.save(product));
    }

    @Transactional
    public ProductResponse updateHoverImageUrl(Long id, String hoverImageUrl) {
        Product product = findOrThrow(id);
        if (product.getHoverImageUrl() != null) {
            imageUploadService.deleteImage(product.getHoverImageUrl());
        }
        product.setHoverImageUrl(hoverImageUrl);
        return productMapper.toResponse(productRepository.save(product));
    }

    @Transactional
    public void deleteImageUrl(Long id) {
        Product product = findOrThrow(id);
        imageUploadService.deleteImage(product.getImageUrl());
        product.setImageUrl(null);
        productRepository.save(product);
    }

    @Transactional
    public void deleteHoverImageUrl(Long id) {
        Product product = findOrThrow(id);
        imageUploadService.deleteImage(product.getHoverImageUrl());
        product.setHoverImageUrl(null);
        productRepository.save(product);
    }

    // ── Helpers ──────────────────────────────────────────────────────────────

    private Product findOrThrow(Long id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Product not found with id: " + id));
    }

    private void resolveRelations(Product product, Long categoryId, Long subcategoryId, Long brandId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new EntityNotFoundException("Category not found with id: " + categoryId));
        product.setCategory(category);

        if (subcategoryId != null) {
            Subcategory subcategory = subcategoryRepository.findById(subcategoryId)
                    .orElseThrow(() -> new EntityNotFoundException("Subcategory not found with id: " + subcategoryId));
            product.setSubcategory(subcategory);
        } else {
            product.setSubcategory(null);
        }

        if (brandId != null) {
            Brand brand = brandRepository.findById(brandId)
                    .orElseThrow(() -> new EntityNotFoundException("Brand not found with id: " + brandId));
            product.setBrand(brand);
        } else {
            product.setBrand(null);
        }
    }

    public Long getProductCount() {
        return productRepository.count();
    }
}