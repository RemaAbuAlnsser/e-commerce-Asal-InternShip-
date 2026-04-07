package com.asal.ecommerce.service;

import com.asal.ecommerce.dto.ProductColorResponse;
import com.asal.ecommerce.dto.ProductCreateRequest;
import com.asal.ecommerce.dto.ProductUpdateRequest;
import com.asal.ecommerce.dto.ProductResponse;
import com.asal.ecommerce.model.*;
import com.asal.ecommerce.repository.*;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;
import org.springframework.mock.web.MockMultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests")
class ProductServiceTest {

    // ── Mocks ──────────────────────────────────────────────────────────────────
    @Mock private ProductRepository           productRepo;
    @Mock private ProductColorRepository      colorRepo;
    @Mock private ProductColorImageRepository colorImageRepo;
    @Mock private CategoryRepository          categoryRepo;
    @Mock private SubcategoryRepository       subcategoryRepo;
    @Mock private BrandRepository             brandRepo;
    @Mock private ImageUploadService          imageUploadService;

    @InjectMocks
    private ProductService productService;

    // ── Shared test fixtures ───────────────────────────────────────────────────
    private Category    testCategory;
    private Subcategory testSubcategory;
    private Brand       testBrand;
    private Product     testProduct;
    private ProductColor testColor;

    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Electronics");

        testSubcategory = new Subcategory();
        testSubcategory.setId(2L);
        testSubcategory.setName("Phones");

        testBrand = new Brand();
        testBrand.setId(3L);
        testBrand.setName("Samsung");

        testColor = ProductColor.builder()
                .id(10L)
                .colorName("Black")
                .colorHex("#000000")
                .stock(50)
                .images(new ArrayList<>())
                .build();

        testProduct = Product.builder()
                .id(1L)
                .name("Test Product")
                .sku("TESTPR-ABC123")
                .description("A test product")
                .price(new BigDecimal("99.99"))
                .oldPrice(new BigDecimal("129.99"))
                .status("active")
                .featured(false)
                .exclusive(false)
                .category(testCategory)
                .subcategory(testSubcategory)
                .brand(testBrand)
                .imageUrl("/uploads/products/prime.jpg")
                .hoverImageUrl("/uploads/products/hover.jpg")
                .colors(new ArrayList<>(List.of(testColor)))
                .build();

        testColor.setProduct(testProduct);
    }

    // =========================================================================
    // CREATE PRODUCT
    // =========================================================================

    @Nested
    @DisplayName("createProduct()")
    class CreateProductTests {

        @Test
        @DisplayName("should create product successfully with all fields")
        void createProduct_withAllFields_returnsProductResponse() throws IOException {
            // given
            ProductCreateRequest req = buildCreateRequest();
            MockMultipartFile primeImage = mockImageFile("prime.jpg");
            MockMultipartFile hoverImage = mockImageFile("hover.jpg");

            when(categoryRepo.findById(1L)).thenReturn(Optional.of(testCategory));
            when(subcategoryRepo.findById(2L)).thenReturn(Optional.of(testSubcategory));
            when(brandRepo.findById(3L)).thenReturn(Optional.of(testBrand));
            when(imageUploadService.uploadProductImage(any())).thenReturn("/uploads/products/prime.jpg");
            when(imageUploadService.uploadProductHoverImage(any())).thenReturn("/uploads/products/hover.jpg");
            when(productRepo.save(any(Product.class))).thenAnswer(inv -> {
                Product p = inv.getArgument(0);
                p.setId(1L);
                return p;
            });

            // when
            ProductResponse result = productService.createProduct(
                    req, primeImage, hoverImage,
                    List.of("Black"), List.of("#000000"), List.of(50),
                    List.of(List.of())
            );

            // then
            assertThat(result).isNotNull();
            assertThat(result.getName()).isEqualTo("Test Product");
            assertThat(result.getPrice()).isEqualByComparingTo("99.99");
            assertThat(result.getCategoryName()).isEqualTo("Electronics");
            assertThat(result.getImageUrl()).isEqualTo("/uploads/products/prime.jpg");
            assertThat(result.getColors()).hasSize(1);
            assertThat(result.getColors().get(0).getColorName()).isEqualTo("Black");
            assertThat(result.getColors().get(0).getStock()).isEqualTo(50);
            assertThat(result.getTotalStock()).isEqualTo(50);

            verify(productRepo).save(any(Product.class));
            verify(imageUploadService).uploadProductImage(primeImage);
            verify(imageUploadService).uploadProductHoverImage(hoverImage);
        }

        @Test
        @DisplayName("should create product without optional fields (no subcategory, brand, images)")
        void createProduct_withoutOptionalFields_succeeds() throws IOException {
            // given
            ProductCreateRequest req = buildCreateRequest();
            req.setSubcategoryId(null);
            req.setBrandId(null);

            when(categoryRepo.findById(1L)).thenReturn(Optional.of(testCategory));
            when(productRepo.save(any())).thenAnswer(inv -> {
                Product p = inv.getArgument(0);
                p.setId(2L);
                return p;
            });

            // when
            ProductResponse result = productService.createProduct(
                    req, null, null,
                    List.of(), List.of(), List.of(), List.of()
            );

            // then
            assertThat(result).isNotNull();
            assertThat(result.getSubcategoryId()).isNull();
            assertThat(result.getBrandId()).isNull();
            assertThat(result.getImageUrl()).isNull();
            assertThat(result.getColors()).isEmpty();
            assertThat(result.getTotalStock()).isEqualTo(0);

            verify(subcategoryRepo, never()).findById(any());
            verify(brandRepo, never()).findById(any());
        }

        @Test
        @DisplayName("should create product with multiple colors and sub-images")
        void createProduct_withMultipleColors_savesAllColors() throws IOException {
            // given
            ProductCreateRequest req = buildCreateRequest();
            MockMultipartFile subImg1 = mockImageFile("sub1.jpg");
            MockMultipartFile subImg2 = mockImageFile("sub2.jpg");

            when(categoryRepo.findById(1L)).thenReturn(Optional.of(testCategory));
            when(subcategoryRepo.findById(2L)).thenReturn(Optional.of(testSubcategory));
            when(brandRepo.findById(3L)).thenReturn(Optional.of(testBrand));
            when(imageUploadService.uploadColorImage(any()))
                    .thenReturn("/uploads/products/colors/sub1.jpg")
                    .thenReturn("/uploads/products/colors/sub2.jpg");
            when(productRepo.save(any())).thenAnswer(inv -> {
                Product p = inv.getArgument(0);
                p.setId(1L);
                return p;
            });

            // when
            ProductResponse result = productService.createProduct(
                    req, null, null,
                    List.of("Black", "White"),
                    List.of("#000000", "#FFFFFF"),
                    List.of(30, 20),
                    List.of(List.of(subImg1), List.of(subImg2))
            );

            // then
            assertThat(result.getColors()).hasSize(2);
            assertThat(result.getTotalStock()).isEqualTo(50); // 30 + 20
            assertThat(result.getColors().get(0).getColorName()).isEqualTo("Black");
            assertThat(result.getColors().get(1).getColorName()).isEqualTo("White");
        }

        @Test
        @DisplayName("should throw RuntimeException when category not found")
        void createProduct_categoryNotFound_throwsException() {
            // given
            ProductCreateRequest req = buildCreateRequest();
            when(categoryRepo.findById(99L)).thenReturn(Optional.empty());
            req.setCategoryId(99L);

            // then
            assertThatThrownBy(() ->
                    productService.createProduct(req, null, null, null, null, null, null)
            ).isInstanceOf(RuntimeException.class)
             .hasMessageContaining("Category not found: 99");

            verify(productRepo, never()).save(any());
        }

        @Test
        @DisplayName("should throw RuntimeException when subcategory not found")
        void createProduct_subcategoryNotFound_throwsException() {
            // given
            ProductCreateRequest req = buildCreateRequest();
            req.setSubcategoryId(99L);
            when(categoryRepo.findById(1L)).thenReturn(Optional.of(testCategory));
            when(subcategoryRepo.findById(99L)).thenReturn(Optional.empty());

            // then
            assertThatThrownBy(() ->
                    productService.createProduct(req, null, null, null, null, null, null)
            ).isInstanceOf(RuntimeException.class)
             .hasMessageContaining("Subcategory not found: 99");
        }

        @Test
        @DisplayName("should generate a SKU automatically on create")
        void createProduct_generatesSku() throws IOException {
            // given
            ProductCreateRequest req = buildCreateRequest();
            req.setName("Oxford Shoe");
            when(categoryRepo.findById(1L)).thenReturn(Optional.of(testCategory));
            when(subcategoryRepo.findById(2L)).thenReturn(Optional.of(testSubcategory));
            when(brandRepo.findById(3L)).thenReturn(Optional.of(testBrand));
            when(productRepo.save(any())).thenAnswer(inv -> {
                Product p = inv.getArgument(0);
                p.setId(1L);
                return p;
            });

            // when
            ProductResponse result = productService.createProduct(
                    req, null, null, null, null, null, null);

            // then — SKU format: XXXXXX-XXXXXX
            assertThat(result.getSku()).isNotBlank();
            assertThat(result.getSku()).matches("^[A-Z0-9]{1,6}-[A-Z0-9]{6}$");
        }

        @Test
        @DisplayName("should default status to 'active' when not provided")
        void createProduct_nullStatus_defaultsToActive() throws IOException {
            // given
            ProductCreateRequest req = buildCreateRequest();
            req.setStatus(null);
            when(categoryRepo.findById(1L)).thenReturn(Optional.of(testCategory));
            when(subcategoryRepo.findById(2L)).thenReturn(Optional.of(testSubcategory));
            when(brandRepo.findById(3L)).thenReturn(Optional.of(testBrand));
            when(productRepo.save(any())).thenAnswer(inv -> {
                Product p = inv.getArgument(0);
                p.setId(1L);
                return p;
            });

            // when
            ProductResponse result = productService.createProduct(
                    req, null, null, null, null, null, null);

            // then
            assertThat(result.getStatus()).isEqualTo("active");
        }
    }

    // =========================================================================
    // GET ALL PRODUCTS (admin)
    // =========================================================================

    @Nested
    @DisplayName("getAllProducts()")
    class GetAllProductsTests {

        @Test
        @DisplayName("should return all products as list")
        void getAllProducts_returnsAllProducts() {
            // given
            Product second = Product.builder()
                    .id(2L).name("Second").sku("SEC-001")
                    .price(new BigDecimal("50.00")).status("active")
                    .category(testCategory).colors(new ArrayList<>()).build();

            when(productRepo.findAll()).thenReturn(List.of(testProduct, second));

            // when
            List<ProductResponse> result = productService.getAllProducts();

            // then
            assertThat(result).hasSize(2);
            assertThat(result).extracting(ProductResponse::getName)
                    .containsExactly("Test Product", "Second");
        }

        @Test
        @DisplayName("should return empty list when no products exist")
        void getAllProducts_noProducts_returnsEmptyList() {
            when(productRepo.findAll()).thenReturn(Collections.emptyList());

            List<ProductResponse> result = productService.getAllProducts();

            assertThat(result).isEmpty();
        }
    }

    // =========================================================================
    // GET PRODUCT BY ID (admin)
    // =========================================================================

    @Nested
    @DisplayName("getProductById()")
    class GetProductByIdTests {

        @Test
        @DisplayName("should return product when found")
        void getProductById_found_returnsProduct() {
            when(productRepo.findById(1L)).thenReturn(Optional.of(testProduct));

            ProductResponse result = productService.getProductById(1L);

            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("Test Product");
            assertThat(result.getTotalStock()).isEqualTo(50);
        }

        @Test
        @DisplayName("should throw RuntimeException when not found")
        void getProductById_notFound_throwsException() {
            when(productRepo.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.getProductById(99L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Product not found: 99");
        }
    }

    // =========================================================================
    // CUSTOMER — GET BY ID
    // =========================================================================

    @Nested
    @DisplayName("getById() — customer")
    class CustomerGetByIdTests {

        @Test
        @DisplayName("should return active product")
        void getById_activeProduct_returnsProduct() {
            when(productRepo.findById(1L)).thenReturn(Optional.of(testProduct));

            ProductResponse result = productService.getById(1L);

            assertThat(result.getId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("should throw when product is inactive")
        void getById_inactiveProduct_throwsException() {
            testProduct.setStatus("inactive");
            when(productRepo.findById(1L)).thenReturn(Optional.of(testProduct));

            assertThatThrownBy(() -> productService.getById(1L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("not available");
        }

        @Test
        @DisplayName("should throw when product not found")
        void getById_notFound_throwsException() {
            when(productRepo.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.getById(99L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Product not found: 99");
        }
    }

    // =========================================================================
    // CUSTOMER — GET BY SKU
    // =========================================================================

    @Nested
    @DisplayName("getBySku() — customer")
    class GetBySkuTests {

        @Test
        @DisplayName("should return active product by SKU")
        void getBySku_activeProduct_returnsProduct() {
            when(productRepo.findBySku("TESTPR-ABC123")).thenReturn(Optional.of(testProduct));

            ProductResponse result = productService.getBySku("TESTPR-ABC123");

            assertThat(result.getSku()).isEqualTo("TESTPR-ABC123");
        }

        @Test
        @DisplayName("should throw when SKU not found")
        void getBySku_notFound_throwsException() {
            when(productRepo.findBySku("NOEXIST-000")).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.getBySku("NOEXIST-000"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("NOEXIST-000");
        }

        @Test
        @DisplayName("should throw when product with SKU is inactive")
        void getBySku_inactiveProduct_throwsException() {
            testProduct.setStatus("draft");
            when(productRepo.findBySku("TESTPR-ABC123")).thenReturn(Optional.of(testProduct));

            assertThatThrownBy(() -> productService.getBySku("TESTPR-ABC123"))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("not available");
        }
    }

    // =========================================================================
    // CUSTOMER — GET ALL WITH FILTERS
    // =========================================================================

    @Nested
    @DisplayName("getAll() — customer paginated")
    class GetAllPaginatedTests {

        @Test
        @DisplayName("should return paginated products with filters applied")
        void getAll_withFilters_returnsPaginatedResults() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> page = new PageImpl<>(List.of(testProduct), pageable, 1);

            when(productRepo.findAllWithFilters(
                    eq("active"), eq(1L), isNull(), isNull(), isNull(), isNull(), eq(pageable)
            )).thenReturn(page);

            // when
            var result = productService.getAll(1L, null, null, "active", null, null, pageable);

            // then
            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getCategoryId()).isEqualTo(1L);
        }

        @Test
        @DisplayName("should return empty page when no products match filters")
        void getAll_noMatchingProducts_returnsEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Product> emptyPage = Page.empty(pageable);

            when(productRepo.findAllWithFilters(any(), any(), any(), any(), any(), any(), any()))
                    .thenReturn(emptyPage);

            var result = productService.getAll(null, null, null, "active", null, null, pageable);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
        }
    }

    // =========================================================================
    // CUSTOMER — SEARCH
    // =========================================================================

    @Nested
    @DisplayName("search()")
    class SearchTests {

        @Test
        @DisplayName("should return matching products by keyword")
        void search_matchingKeyword_returnsProducts() {
            Pageable pageable = PageRequest.of(0, 20);
            Page<Product> page = new PageImpl<>(List.of(testProduct), pageable, 1);

            when(productRepo.searchByKeyword(eq("test"), eq(pageable))).thenReturn(page);

            var result = productService.search("test", pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getName()).isEqualTo("Test Product");
        }

        @Test
        @DisplayName("should return empty page when no keyword matches")
        void search_noMatch_returnsEmptyPage() {
            Pageable pageable = PageRequest.of(0, 20);
            when(productRepo.searchByKeyword(eq("xyz"), eq(pageable)))
                    .thenReturn(Page.empty(pageable));

            var result = productService.search("xyz", pageable);

            assertThat(result.getContent()).isEmpty();
        }
    }

    // =========================================================================
    // UPDATE PRODUCT
    // =========================================================================

    @Nested
    @DisplayName("updateProduct()")
    class UpdateProductTests {

        @Test
        @DisplayName("should update basic fields successfully")
        void updateProduct_basicFields_updatesCorrectly() throws IOException {
            // given
            ProductUpdateRequest req = new ProductUpdateRequest();
            req.setName("Updated Name");
            req.setPrice(new BigDecimal("149.99"));
            req.setStatus("inactive");
            req.setFeatured(true);
            req.setExclusive(false);

            when(productRepo.findById(1L)).thenReturn(Optional.of(testProduct));
            when(productRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // when
            ProductResponse result = productService.updateProduct(1L, req, null, null);

            // then
            assertThat(result.getName()).isEqualTo("Updated Name");
            assertThat(result.getPrice()).isEqualByComparingTo("149.99");
            assertThat(result.getStatus()).isEqualTo("inactive");
            assertThat(result.isFeatured()).isTrue();
        }

        @Test
        @DisplayName("should replace prime image when new one provided")
        void updateProduct_withNewPrimeImage_replacesImage() throws IOException {
            // given
            ProductUpdateRequest req = new ProductUpdateRequest();
            req.setFeatured(false);
            req.setExclusive(false);
            MockMultipartFile newImage = mockImageFile("new-prime.jpg");

            when(productRepo.findById(1L)).thenReturn(Optional.of(testProduct));
            when(imageUploadService.uploadProductImage(any()))
                    .thenReturn("/uploads/products/new-prime.jpg");
            when(productRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // when
            ProductResponse result = productService.updateProduct(1L, req, newImage, null);

            // then
            assertThat(result.getImageUrl()).isEqualTo("/uploads/products/new-prime.jpg");
            verify(imageUploadService).deleteImage("/uploads/products/prime.jpg");
            verify(imageUploadService).uploadProductImage(newImage);
        }

        @Test
        @DisplayName("should not replace image when no new image provided")
        void updateProduct_withoutNewImage_keepsExistingImage() throws IOException {
            // given
            ProductUpdateRequest req = new ProductUpdateRequest();
            req.setName("New Name");
            req.setFeatured(false);
            req.setExclusive(false);

            when(productRepo.findById(1L)).thenReturn(Optional.of(testProduct));
            when(productRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // when
            productService.updateProduct(1L, req, null, null);

            // then
            verify(imageUploadService, never()).uploadProductImage(any());
            verify(imageUploadService, never()).deleteImage(any());
        }

        @Test
        @DisplayName("should throw when product to update is not found")
        void updateProduct_notFound_throwsException() {
            when(productRepo.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    productService.updateProduct(99L, new ProductUpdateRequest(), null, null)
            ).isInstanceOf(RuntimeException.class)
             .hasMessageContaining("Product not found: 99");
        }

        @Test
        @DisplayName("should update category when new categoryId provided")
        void updateProduct_withNewCategory_updatesCategory() throws IOException {
            // given
            Category newCategory = new Category();
            newCategory.setId(5L);
            newCategory.setName("Fashion");

            ProductUpdateRequest req = new ProductUpdateRequest();
            req.setCategoryId(5L);
            req.setFeatured(false);
            req.setExclusive(false);

            when(productRepo.findById(1L)).thenReturn(Optional.of(testProduct));
            when(categoryRepo.findById(5L)).thenReturn(Optional.of(newCategory));
            when(productRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // when
            ProductResponse result = productService.updateProduct(1L, req, null, null);

            // then
            assertThat(result.getCategoryName()).isEqualTo("Fashion");
        }
    }

    // =========================================================================
    // ADD COLOR TO EXISTING PRODUCT
    // =========================================================================

    @Nested
    @DisplayName("addColorToProduct()")
    class AddColorTests {

        @Test
        @DisplayName("should add new color variant to product")
        void addColorToProduct_validData_addsColor() throws IOException {
            // given
            when(productRepo.findById(1L)).thenReturn(Optional.of(testProduct));
            when(productRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // when
            ProductResponse result = productService.addColorToProduct(
                    1L, "Red", "#FF0000", 25, List.of()
            );

            // then
            assertThat(result.getColors()).hasSize(2); // Black (existing) + Red (new)
            assertThat(result.getColors())
                    .extracting(c -> c.getColorName())
                    .contains("Red");
            assertThat(result.getTotalStock()).isEqualTo(75); // 50 + 25
        }

        @Test
        @DisplayName("should upload sub-images when adding color")
        void addColorToProduct_withSubImages_uploadsImages() throws IOException {
            // given
            MockMultipartFile img1 = mockImageFile("red1.jpg");
            MockMultipartFile img2 = mockImageFile("red2.jpg");

            when(productRepo.findById(1L)).thenReturn(Optional.of(testProduct));
            when(imageUploadService.uploadColorImage(img1)).thenReturn("/uploads/products/colors/red1.jpg");
            when(imageUploadService.uploadColorImage(img2)).thenReturn("/uploads/products/colors/red2.jpg");
            when(productRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // when
            ProductResponse result = productService.addColorToProduct(
                    1L, "Red", "#FF0000", 25, List.of(img1, img2)
            );

            // then
            ProductColorResponse newColor = result.getColors().stream()
                    .filter(c -> c.getColorName().equals("Red"))
                    .findFirst().orElseThrow();

            assertThat(newColor.getImages()).hasSize(2);
            verify(imageUploadService, times(2)).uploadColorImage(any());
        }

        @Test
        @DisplayName("should throw when product not found")
        void addColorToProduct_productNotFound_throwsException() {
            when(productRepo.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() ->
                    productService.addColorToProduct(99L, "Blue", "#0000FF", 10, List.of())
            ).isInstanceOf(RuntimeException.class)
             .hasMessageContaining("Product not found: 99");
        }
    }

    // =========================================================================
    // UPDATE COLOR STOCK
    // =========================================================================

    @Nested
    @DisplayName("updateColorStock()")
    class UpdateColorStockTests {

        @Test
        @DisplayName("should update stock of specific color")
        void updateColorStock_validIds_updatesStock() {
            // given
            when(productRepo.findById(1L)).thenReturn(Optional.of(testProduct));
            when(colorRepo.findById(10L)).thenReturn(Optional.of(testColor));
            when(colorRepo.save(any())).thenReturn(testColor);
            when(productRepo.findById(1L)).thenReturn(Optional.of(testProduct));

            // when
            ProductResponse result = productService.updateColorStock(1L, 10L, 100);

            // then
            assertThat(result.getColors().get(0).getStock()).isEqualTo(100);
            verify(colorRepo).save(testColor);
        }

        @Test
        @DisplayName("should allow setting stock to zero")
        void updateColorStock_toZero_succeeds() {
            when(productRepo.findById(1L)).thenReturn(Optional.of(testProduct));
            when(colorRepo.findById(10L)).thenReturn(Optional.of(testColor));
            when(colorRepo.save(any())).thenReturn(testColor);

            ProductResponse result = productService.updateColorStock(1L, 10L, 0);

            assertThat(result.getTotalStock()).isEqualTo(0);
        }

        @Test
        @DisplayName("should throw when color does not belong to product")
        void updateColorStock_colorNotBelongingToProduct_throwsException() {
            // given — color belongs to product 99, not product 1
            Product otherProduct = Product.builder().id(99L).build();
            testColor.setProduct(otherProduct);

            when(productRepo.findById(1L)).thenReturn(Optional.of(testProduct));
            when(colorRepo.findById(10L)).thenReturn(Optional.of(testColor));

            // then
            assertThatThrownBy(() -> productService.updateColorStock(1L, 10L, 50))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("does not belong to this product");
        }

        @Test
        @DisplayName("should throw when color not found")
        void updateColorStock_colorNotFound_throwsException() {
            when(productRepo.findById(1L)).thenReturn(Optional.of(testProduct));
            when(colorRepo.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.updateColorStock(1L, 99L, 50))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Color not found: 99");
        }
    }

    // =========================================================================
    // DELETE COLOR
    // =========================================================================

    @Nested
    @DisplayName("deleteColor()")
    class DeleteColorTests {

        @Test
        @DisplayName("should delete color and its images from disk")
        void deleteColor_validIds_deletesColorAndImages() {
            // given
            ProductColorImage img = ProductColorImage.builder()
                    .id(100L)
                    .imageUrl("/uploads/products/colors/black1.jpg")
                    .productColor(testColor)
                    .build();
            testColor.setImages(new ArrayList<>(List.of(img)));

            when(productRepo.findById(1L)).thenReturn(Optional.of(testProduct));
            when(colorRepo.findById(10L)).thenReturn(Optional.of(testColor));
            when(productRepo.findById(1L)).thenReturn(Optional.of(testProduct));

            // when
            productService.deleteColor(1L, 10L);

            // then
            verify(imageUploadService).deleteImage("/uploads/products/colors/black1.jpg");
            verify(colorRepo).delete(testColor);
        }

        @Test
        @DisplayName("should throw when color does not belong to product")
        void deleteColor_wrongProduct_throwsException() {
            Product otherProduct = Product.builder().id(99L).build();
            testColor.setProduct(otherProduct);

            when(productRepo.findById(1L)).thenReturn(Optional.of(testProduct));
            when(colorRepo.findById(10L)).thenReturn(Optional.of(testColor));

            assertThatThrownBy(() -> productService.deleteColor(1L, 10L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("does not belong to this product");

            verify(colorRepo, never()).delete(any());
        }
    }

    // =========================================================================
    // DELETE PRODUCT
    // =========================================================================

    @Nested
    @DisplayName("deleteProduct()")
    class DeleteProductTests {

        @Test
        @DisplayName("should delete product and all associated images")
        void deleteProduct_validId_deletesProductAndImages() {
            // given
            ProductColorImage img = ProductColorImage.builder()
                    .id(100L)
                    .imageUrl("/uploads/products/colors/black1.jpg")
                    .productColor(testColor)
                    .build();
            testColor.setImages(new ArrayList<>(List.of(img)));

            when(productRepo.findById(1L)).thenReturn(Optional.of(testProduct));

            // when
            productService.deleteProduct(1L);

            // then
            verify(imageUploadService).deleteImage("/uploads/products/prime.jpg");
            verify(imageUploadService).deleteImage("/uploads/products/hover.jpg");
            verify(imageUploadService).deleteImage("/uploads/products/colors/black1.jpg");
            verify(productRepo).delete(testProduct);
        }

        @Test
        @DisplayName("should throw when product not found")
        void deleteProduct_notFound_throwsException() {
            when(productRepo.findById(99L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> productService.deleteProduct(99L))
                    .isInstanceOf(RuntimeException.class)
                    .hasMessageContaining("Product not found: 99");

            verify(productRepo, never()).delete(any());
        }

        @Test
        @DisplayName("should handle products with no images gracefully")
        void deleteProduct_noImages_deletesCleanly() {
            testProduct.setImageUrl(null);
            testProduct.setHoverImageUrl(null);
            testColor.setImages(new ArrayList<>());

            when(productRepo.findById(1L)).thenReturn(Optional.of(testProduct));

            assertThatCode(() -> productService.deleteProduct(1L))
                    .doesNotThrowAnyException();

            verify(productRepo).delete(testProduct);
        }
    }

    // =========================================================================
    // TOTAL STOCK CALCULATION
    // =========================================================================

    @Nested
    @DisplayName("totalStock calculation")
    class TotalStockTests {

        @Test
        @DisplayName("should sum stock across all colors")
        void totalStock_multipleColors_returnsSum() {
            // given
            ProductColor red = ProductColor.builder()
                    .id(20L).colorName("Red").colorHex("#FF0000")
                    .stock(30).images(new ArrayList<>()).product(testProduct).build();
            ProductColor blue = ProductColor.builder()
                    .id(30L).colorName("Blue").colorHex("#0000FF")
                    .stock(20).images(new ArrayList<>()).product(testProduct).build();
            testProduct.setColors(new ArrayList<>(List.of(testColor, red, blue))); // 50+30+20=100

            when(productRepo.findById(1L)).thenReturn(Optional.of(testProduct));

            // when
            ProductResponse result = productService.getProductById(1L);

            // then
            assertThat(result.getTotalStock()).isEqualTo(100);
        }

        @Test
        @DisplayName("should return zero when product has no colors")
        void totalStock_noColors_returnsZero() {
            testProduct.setColors(new ArrayList<>());
            when(productRepo.findById(1L)).thenReturn(Optional.of(testProduct));

            ProductResponse result = productService.getProductById(1L);

            assertThat(result.getTotalStock()).isEqualTo(0);
        }
    }

    // =========================================================================
    // HELPERS
    // =========================================================================

    private ProductCreateRequest buildCreateRequest() {
        ProductCreateRequest req = new ProductCreateRequest();
        req.setName("Test Product");
        req.setDescription("A test product");
        req.setPrice(new BigDecimal("99.99"));
        req.setOldPrice(new BigDecimal("129.99"));
        req.setStatus("active");
        req.setFeatured(false);
        req.setExclusive(false);
        req.setCategoryId(1L);
        req.setSubcategoryId(2L);
        req.setBrandId(3L);
        return req;
    }

    private MockMultipartFile mockImageFile(String filename) {
        return new MockMultipartFile(
                "file", filename, "image/jpeg", "fake-image-bytes".getBytes()
        );
    }
}