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
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService")
class ProductServiceTest {

    @Mock ProductRepository     productRepository;
    @Mock CategoryRepository    categoryRepository;
    @Mock SubcategoryRepository subcategoryRepository;
    @Mock BrandRepository       brandRepository;
    @Mock ProductMapper         productMapper;
    @Mock ImageUploadService    imageUploadService;

    @InjectMocks
    ProductService productService;

    // ── Shared fixtures ───────────────────────────────────────────────────────

    private Category    category;
    private Subcategory subcategory;
    private Brand       brand;
    private Product     product;
    private ProductResponse productResponse;

    @BeforeEach
    void setUp() {
        category = new Category();
        category.setId(1L);
        category.setName("Electronics");

        subcategory = new Subcategory();
        subcategory.setId(2L);
        subcategory.setName("Phones");

        brand = new Brand();
        brand.setId(3L);
        brand.setName("Samsung");

        product = Product.builder()
                .id(10L)
                .name("Galaxy S24")
                .sku("SAM-S24-001")
                .price(new BigDecimal("999.99"))
                .stock(50)
                .status("active")
                .isFeatured(false)
                .isExclusive(false)
                .category(category)
                .subcategory(subcategory)
                .brand(brand)
                .build();

        productResponse = new ProductResponse();
        productResponse.setId(10L);
        productResponse.setName("Galaxy S24");
        productResponse.setSku("SAM-S24-001");
        productResponse.setPrice(new BigDecimal("999.99"));
        productResponse.setStock(50);
        productResponse.setStatus("active");
        productResponse.setCategoryId(1L);
        productResponse.setCategoryName("Electronics");
        productResponse.setSubcategoryId(2L);
        productResponse.setSubcategoryName("Phones");
        productResponse.setBrandId(3L);
        productResponse.setBrandName("Samsung");
    }

    // =========================================================================
    // CREATE
    // =========================================================================

    @Nested
    @DisplayName("create()")
    class Create {

        private ProductCreateRequest buildRequest() {
            ProductCreateRequest req = new ProductCreateRequest();
            req.setName("Galaxy S24");
            req.setSku("SAM-S24-001");
            req.setPrice(new BigDecimal("999.99"));
            req.setStock(50);
            req.setStatus("active");
            req.setCategoryId(1L);
            req.setSubcategoryId(2L);
            req.setBrandId(3L);
            return req;
        }

        @Test
        @DisplayName("saves product and returns response when SKU is unique")
        void create_success() {
            ProductCreateRequest req = buildRequest();
            given(productRepository.existsBySku("SAM-S24-001")).willReturn(false);
            given(productMapper.toEntity(req)).willReturn(product);
            given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
            given(subcategoryRepository.findById(2L)).willReturn(Optional.of(subcategory));
            given(brandRepository.findById(3L)).willReturn(Optional.of(brand));
            given(productRepository.save(product)).willReturn(product);
            given(productMapper.toResponse(product)).willReturn(productResponse);

            ProductResponse result = productService.create(req);

            assertThat(result).isNotNull();
            assertThat(result.getSku()).isEqualTo("SAM-S24-001");
            assertThat(result.getCategoryName()).isEqualTo("Electronics");
            then(productRepository).should().save(product);
        }

        @Test
        @DisplayName("throws IllegalArgumentException when SKU already exists")
        void create_duplicateSku_throws() {
            ProductCreateRequest req = buildRequest();
            given(productRepository.existsBySku("SAM-S24-001")).willReturn(true);

            assertThatThrownBy(() -> productService.create(req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("SAM-S24-001");

            then(productRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("throws EntityNotFoundException when category does not exist")
        void create_categoryNotFound_throws() {
            ProductCreateRequest req = buildRequest();
            given(productRepository.existsBySku(any())).willReturn(false);
            given(productMapper.toEntity(req)).willReturn(product);
            given(categoryRepository.findById(1L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> productService.create(req))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Category");
        }

        @Test
        @DisplayName("throws EntityNotFoundException when subcategory does not exist")
        void create_subcategoryNotFound_throws() {
            ProductCreateRequest req = buildRequest();
            given(productRepository.existsBySku(any())).willReturn(false);
            given(productMapper.toEntity(req)).willReturn(product);
            given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
            given(subcategoryRepository.findById(2L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> productService.create(req))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Subcategory");
        }

        @Test
        @DisplayName("throws EntityNotFoundException when brand does not exist")
        void create_brandNotFound_throws() {
            ProductCreateRequest req = buildRequest();
            given(productRepository.existsBySku(any())).willReturn(false);
            given(productMapper.toEntity(req)).willReturn(product);
            given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
            given(subcategoryRepository.findById(2L)).willReturn(Optional.of(subcategory));
            given(brandRepository.findById(3L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> productService.create(req))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("Brand");
        }

        @Test
        @DisplayName("creates product without optional subcategory and brand")
        void create_withoutSubcategoryAndBrand_success() {
            ProductCreateRequest req = buildRequest();
            req.setSubcategoryId(null);
            req.setBrandId(null);

            product.setSubcategory(null);
            product.setBrand(null);

            given(productRepository.existsBySku(any())).willReturn(false);
            given(productMapper.toEntity(req)).willReturn(product);
            given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
            given(productRepository.save(product)).willReturn(product);
            given(productMapper.toResponse(product)).willReturn(productResponse);

            ProductResponse result = productService.create(req);

            assertThat(result).isNotNull();
            then(subcategoryRepository).should(never()).findById(any());
            then(brandRepository).should(never()).findById(any());
        }
    }

    // =========================================================================
    // READ
    // =========================================================================

    @Nested
    @DisplayName("getById()")
    class GetById {

        @Test
        @DisplayName("returns response when product exists")
        void getById_success() {
            given(productRepository.findById(10L)).willReturn(Optional.of(product));
            given(productMapper.toResponse(product)).willReturn(productResponse);

            ProductResponse result = productService.getById(10L);

            assertThat(result.getId()).isEqualTo(10L);
        }

        @Test
        @DisplayName("throws EntityNotFoundException when product not found")
        void getById_notFound_throws() {
            given(productRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> productService.getById(99L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("99");
        }
    }

    @Nested
    @DisplayName("getBySku()")
    class GetBySku {

        @Test
        @DisplayName("returns response when SKU matches")
        void getBySku_success() {
            given(productRepository.findBySku("SAM-S24-001")).willReturn(Optional.of(product));
            given(productMapper.toResponse(product)).willReturn(productResponse);

            ProductResponse result = productService.getBySku("SAM-S24-001");

            assertThat(result.getSku()).isEqualTo("SAM-S24-001");
        }

        @Test
        @DisplayName("throws EntityNotFoundException when SKU not found")
        void getBySku_notFound_throws() {
            given(productRepository.findBySku("UNKNOWN")).willReturn(Optional.empty());

            assertThatThrownBy(() -> productService.getBySku("UNKNOWN"))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("UNKNOWN");
        }
    }

    @Nested
    @DisplayName("getAll()")
    class GetAll {

        @Test
        @DisplayName("returns paged results with all filters applied")
        void getAll_withFilters_success() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<Product> page = new PageImpl<>(List.of(product));

            given(productRepository.findAllFiltered(1L, 2L, 3L, "active", true, false, pageable))
                    .willReturn(page);
            given(productMapper.toResponse(product)).willReturn(productResponse);

            Page<ProductResponse> result = productService.getAll(1L, 2L, 3L, "active", true, false, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getName()).isEqualTo("Galaxy S24");
        }

        @Test
        @DisplayName("returns all products when no filters given")
        void getAll_noFilters_returnsAll() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<Product> page = new PageImpl<>(List.of(product));

            given(productRepository.findAllFiltered(null, null, null, null, null, null, pageable))
                    .willReturn(page);
            given(productMapper.toResponse(product)).willReturn(productResponse);

            Page<ProductResponse> result = productService.getAll(null, null, null, null, null, null, pageable);

            assertThat(result.getTotalElements()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("search()")
    class Search {

        @Test
        @DisplayName("returns matching products by keyword")
        void search_byName_success() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<Product> page = new PageImpl<>(List.of(product));

            given(productRepository.searchByKeyword("galaxy", pageable)).willReturn(page);
            given(productMapper.toResponse(product)).willReturn(productResponse);

            Page<ProductResponse> result = productService.search("galaxy", pageable);

            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("returns empty page when keyword matches nothing")
        void search_noMatch_returnsEmpty() {
            Pageable pageable = PageRequest.of(0, 20);
            given(productRepository.searchByKeyword("zzz", pageable)).willReturn(Page.empty());

            Page<ProductResponse> result = productService.search("zzz", pageable);

            assertThat(result.getContent()).isEmpty();
        }
    }

    // =========================================================================
    // UPDATE
    // =========================================================================

    @Nested
    @DisplayName("update()")
    class Update {

        private ProductUpdateRequest buildUpdateRequest() {
            ProductUpdateRequest req = new ProductUpdateRequest();
            req.setName("Galaxy S24 Ultra");
            req.setSku("SAM-S24-001");      // same SKU — allowed
            req.setPrice(new BigDecimal("1199.99"));
            req.setStock(30);
            req.setStatus("active");
            req.setCategoryId(1L);
            req.setSubcategoryId(2L);
            req.setBrandId(3L);
            return req;
        }

        @Test
        @DisplayName("updates and returns product when SKU is unchanged")
        void update_sameSku_success() {
            ProductUpdateRequest req = buildUpdateRequest();
            given(productRepository.findById(10L)).willReturn(Optional.of(product));
            given(productRepository.existsBySkuAndIdNot("SAM-S24-001", 10L)).willReturn(false);
            given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
            given(subcategoryRepository.findById(2L)).willReturn(Optional.of(subcategory));
            given(brandRepository.findById(3L)).willReturn(Optional.of(brand));
            given(productRepository.save(product)).willReturn(product);
            given(productMapper.toResponse(product)).willReturn(productResponse);

            ProductResponse result = productService.update(10L, req);

            assertThat(result).isNotNull();
            then(productMapper).should().updateEntity(req, product);
        }

        @Test
        @DisplayName("throws IllegalArgumentException when new SKU belongs to another product")
        void update_duplicateSku_throws() {
            ProductUpdateRequest req = buildUpdateRequest();
            req.setSku("OTHER-SKU");
            given(productRepository.findById(10L)).willReturn(Optional.of(product));
            given(productRepository.existsBySkuAndIdNot("OTHER-SKU", 10L)).willReturn(true);

            assertThatThrownBy(() -> productService.update(10L, req))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("OTHER-SKU");

            then(productRepository).should(never()).save(any());
        }

        @Test
        @DisplayName("throws EntityNotFoundException when product to update does not exist")
        void update_productNotFound_throws() {
            ProductUpdateRequest req = buildUpdateRequest();
            given(productRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> productService.update(99L, req))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("99");
        }

        @Test
        @DisplayName("clears subcategory and brand when set to null in request")
        void update_clearOptionalRelations_success() {
            ProductUpdateRequest req = buildUpdateRequest();
            req.setSubcategoryId(null);
            req.setBrandId(null);

            given(productRepository.findById(10L)).willReturn(Optional.of(product));
            given(productRepository.existsBySkuAndIdNot(any(), eq(10L))).willReturn(false);
            given(categoryRepository.findById(1L)).willReturn(Optional.of(category));
            given(productRepository.save(product)).willReturn(product);
            given(productMapper.toResponse(product)).willReturn(productResponse);

            productService.update(10L, req);

            assertThat(product.getSubcategory()).isNull();
            assertThat(product.getBrand()).isNull();
        }
    }

    // =========================================================================
    // DELETE
    // =========================================================================

    @Nested
    @DisplayName("delete()")
    class Delete {

        @Test
        @DisplayName("deletes product when it exists")
        void delete_success() {
            given(productRepository.existsById(10L)).willReturn(true);

            productService.delete(10L);

            then(productRepository).should().deleteById(10L);
        }

        @Test
        @DisplayName("throws EntityNotFoundException when product does not exist")
        void delete_notFound_throws() {
            given(productRepository.existsById(99L)).willReturn(false);

            assertThatThrownBy(() -> productService.delete(99L))
                    .isInstanceOf(EntityNotFoundException.class)
                    .hasMessageContaining("99");

            then(productRepository).should(never()).deleteById(any());
        }
    }

    // =========================================================================
    // IMAGE MANAGEMENT
    // =========================================================================

    @Nested
    @DisplayName("updateImageUrl()")
    class UpdateImageUrl {

        @Test
        @DisplayName("deletes old image and saves new URL")
        void updateImageUrl_replacesOldImage() {
            product.setImageUrl("/uploads/products/old.jpg");
            given(productRepository.findById(10L)).willReturn(Optional.of(product));
            given(productRepository.save(product)).willReturn(product);
            given(productMapper.toResponse(product)).willReturn(productResponse);

            productService.updateImageUrl(10L, "/uploads/products/new.jpg");

            then(imageUploadService).should().deleteImage("/uploads/products/old.jpg");
            assertThat(product.getImageUrl()).isEqualTo("/uploads/products/new.jpg");
        }

        @Test
        @DisplayName("skips deletion when product has no existing image")
        void updateImageUrl_noOldImage_skipsDelete() {
            product.setImageUrl(null);
            given(productRepository.findById(10L)).willReturn(Optional.of(product));
            given(productRepository.save(product)).willReturn(product);
            given(productMapper.toResponse(product)).willReturn(productResponse);

            productService.updateImageUrl(10L, "/uploads/products/new.jpg");

            then(imageUploadService).should(never()).deleteImage(any());
        }

        @Test
        @DisplayName("throws EntityNotFoundException when product not found")
        void updateImageUrl_productNotFound_throws() {
            given(productRepository.findById(99L)).willReturn(Optional.empty());

            assertThatThrownBy(() -> productService.updateImageUrl(99L, "/uploads/products/x.jpg"))
                    .isInstanceOf(EntityNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("updateHoverImageUrl()")
    class UpdateHoverImageUrl {

        @Test
        @DisplayName("deletes old hover image and saves new URL")
        void updateHoverImageUrl_replacesOldImage() {
            product.setHoverImageUrl("/uploads/products/old-hover.jpg");
            given(productRepository.findById(10L)).willReturn(Optional.of(product));
            given(productRepository.save(product)).willReturn(product);
            given(productMapper.toResponse(product)).willReturn(productResponse);

            productService.updateHoverImageUrl(10L, "/uploads/products/new-hover.jpg");

            then(imageUploadService).should().deleteImage("/uploads/products/old-hover.jpg");
            assertThat(product.getHoverImageUrl()).isEqualTo("/uploads/products/new-hover.jpg");
        }
    }

    @Nested
    @DisplayName("deleteImageUrl()")
    class DeleteImageUrl {

        @Test
        @DisplayName("deletes file from disk and sets imageUrl to null")
        void deleteImageUrl_success() {
            product.setImageUrl("/uploads/products/img.jpg");
            given(productRepository.findById(10L)).willReturn(Optional.of(product));
            given(productRepository.save(product)).willReturn(product);

            productService.deleteImageUrl(10L);

            then(imageUploadService).should().deleteImage("/uploads/products/img.jpg");
            assertThat(product.getImageUrl()).isNull();
        }
    }

    @Nested
    @DisplayName("deleteHoverImageUrl()")
    class DeleteHoverImageUrl {

        @Test
        @DisplayName("deletes file from disk and sets hoverImageUrl to null")
        void deleteHoverImageUrl_success() {
            product.setHoverImageUrl("/uploads/products/hover.jpg");
            given(productRepository.findById(10L)).willReturn(Optional.of(product));
            given(productRepository.save(product)).willReturn(product);

            productService.deleteHoverImageUrl(10L);

            then(imageUploadService).should().deleteImage("/uploads/products/hover.jpg");
            assertThat(product.getHoverImageUrl()).isNull();
        }
    }
}