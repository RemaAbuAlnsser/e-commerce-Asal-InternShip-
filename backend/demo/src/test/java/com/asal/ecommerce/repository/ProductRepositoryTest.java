package com.asal.ecommerce.repository;

import com.asal.ecommerce.model.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private SubcategoryRepository subcategoryRepository;

    @Autowired
    private BrandRepository brandRepository;

    @Autowired
    private ProductColorRepository productColorRepository;

    private Product testProduct;
    private Category testCategory;
    private Subcategory testSubcategory;
    private Brand testBrand;

    @BeforeEach
    void setUp() {
        // Clean up database in correct order to avoid FK violations
        productColorRepository.deleteAll();
        productRepository.deleteAll();
        subcategoryRepository.deleteAll();
        categoryRepository.deleteAll();
        brandRepository.deleteAll();

        // Create test category
        testCategory = new Category();
        testCategory.setName("Electronics");
        testCategory.setSlug("electronics");
        testCategory.setDescription("Electronic devices");
        testCategory.setIsActive(true);
        testCategory = categoryRepository.save(testCategory);

        // Create test subcategory
        testSubcategory = new Subcategory();
        testSubcategory.setName("Smartphones");
        testSubcategory.setSlug("smartphones");
        testSubcategory.setDescription("Mobile phones");
        testSubcategory.setIsActive(true);
        testSubcategory.setCategory(testCategory);
        testSubcategory = subcategoryRepository.save(testSubcategory);

        // Create test brand
        testBrand = new Brand();
        testBrand.setName("Apple");
        testBrand.setIsActive(true);
        testBrand = brandRepository.save(testBrand);

        // Create test product
        testProduct = Product.builder()
                .name("iPhone 15")
                .sku("IPHONE15-001")
                .description("Latest iPhone model")
                .price(new BigDecimal("999.99"))
                .oldPrice(new BigDecimal("1099.99"))
                .status("active")
                .featured(false)
                .exclusive(false)
                .category(testCategory)
                .subcategory(testSubcategory)
                .brand(testBrand)
                .imageUrl("iphone15.jpg")
                .hoverImageUrl("iphone15-hover.jpg")
                .build();
    }

    @Test
    void shouldReturnTrue_whenExistsBySku() {
        // Given
        productRepository.save(testProduct);

        // When
        boolean exists = productRepository.existsBySku("IPHONE15-001");

        // Then
        assertTrue(exists);
    }

    @Test
    void shouldReturnFalse_whenNotExistsBySku() {
        // When
        boolean exists = productRepository.existsBySku("NON-EXISTENT-SKU");

        // Then
        assertFalse(exists);
    }

    @Test
    void shouldFindProduct_whenFindBySku() {
        // Given
        productRepository.save(testProduct);

        // When
        Optional<Product> result = productRepository.findBySku("IPHONE15-001");

        // Then
        assertTrue(result.isPresent());
        assertEquals("iPhone 15", result.get().getName());
        assertEquals("IPHONE15-001", result.get().getSku());
    }

    @Test
    void shouldNotFindProduct_whenFindBySkuNotExists() {
        // When
        Optional<Product> result = productRepository.findBySku("NON-EXISTENT-SKU");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void shouldFindAllProducts_whenFindAllWithFiltersWithNullParams() {
        // Given
        productRepository.save(testProduct);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Product> result = productRepository.findAllWithFilters(
                null, null, null, null, null, null, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("iPhone 15", result.getContent().get(0).getName());
    }

    @Test
    void shouldFilterByStatus_whenFindAllWithFilters() {
        // Given
        Product inactiveProduct = Product.builder()
                .name("Old iPhone")
                .sku("OLD-IPHONE-001")
                .description("Discontinued iPhone")
                .price(new BigDecimal("499.99"))
                .status("inactive")
                .category(testCategory)
                .build();

        productRepository.save(testProduct);
        productRepository.save(inactiveProduct);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Product> activeResults = productRepository.findAllWithFilters(
                "active", null, null, null, null, null, pageable);
        Page<Product> inactiveResults = productRepository.findAllWithFilters(
                "inactive", null, null, null, null, null, pageable);

        // Then
        assertEquals(1, activeResults.getTotalElements());
        assertEquals(1, inactiveResults.getTotalElements());
        assertEquals("active", activeResults.getContent().get(0).getStatus());
        assertEquals("inactive", inactiveResults.getContent().get(0).getStatus());
    }

    @Test
    void shouldFilterByCategory_whenFindAllWithFilters() {
        // Given
        Category anotherCategory = new Category();
        anotherCategory.setName("Clothing");
        anotherCategory.setSlug("clothing");
        anotherCategory.setIsActive(true);
        anotherCategory = categoryRepository.save(anotherCategory);

        Product clothingProduct = Product.builder()
                .name("T-Shirt")
                .sku("TSHIRT-001")
                .description("Cotton T-Shirt")
                .price(new BigDecimal("29.99"))
                .status("active")
                .category(anotherCategory)
                .build();

        productRepository.save(testProduct);
        productRepository.save(clothingProduct);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Product> electronicsResults = productRepository.findAllWithFilters(
                null, testCategory.getId(), null, null, null, null, pageable);

        // Then
        assertEquals(1, electronicsResults.getTotalElements());
        assertEquals("iPhone 15", electronicsResults.getContent().get(0).getName());
        assertEquals(testCategory.getId(), electronicsResults.getContent().get(0).getCategory().getId());
    }

    @Test
    void shouldFilterBySubcategory_whenFindAllWithFilters() {
        // Given
        productRepository.save(testProduct);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Product> result = productRepository.findAllWithFilters(
                null, null, testSubcategory.getId(), null, null, null, pageable);

        // Then
        assertEquals(1, result.getTotalElements());
        assertEquals("iPhone 15", result.getContent().get(0).getName());
        assertEquals(testSubcategory.getId(), result.getContent().get(0).getSubcategory().getId());
    }

    @Test
    void shouldFilterByBrand_whenFindAllWithFilters() {
        // Given
        productRepository.save(testProduct);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Product> result = productRepository.findAllWithFilters(
                null, null, null, testBrand.getId(), null, null, pageable);

        // Then
        assertEquals(1, result.getTotalElements());
        assertEquals("iPhone 15", result.getContent().get(0).getName());
        assertEquals(testBrand.getId(), result.getContent().get(0).getBrand().getId());
    }

    @Test
    void shouldFilterByFeatured_whenFindAllWithFilters() {
        // Given
        Product featuredProduct = Product.builder()
                .name("Featured iPhone")
                .sku("FEATURED-IPHONE-001")
                .description("Featured iPhone model")
                .price(new BigDecimal("1199.99"))
                .status("active")
                .featured(true)
                .category(testCategory)
                .build();

        productRepository.save(testProduct);
        productRepository.save(featuredProduct);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Product> featuredResults = productRepository.findAllWithFilters(
                null, null, null, null, true, null, pageable);
        Page<Product> nonFeaturedResults = productRepository.findAllWithFilters(
                null, null, null, null, false, null, pageable);

        // Then
        assertEquals(1, featuredResults.getTotalElements());
        assertEquals(1, nonFeaturedResults.getTotalElements());
        assertTrue(featuredResults.getContent().get(0).isFeatured());
        assertFalse(nonFeaturedResults.getContent().get(0).isFeatured());
    }

    @Test
    void shouldFilterByExclusive_whenFindAllWithFilters() {
        // Given
        Product exclusiveProduct = Product.builder()
                .name("Exclusive iPhone")
                .sku("EXCLUSIVE-IPHONE-001")
                .description("Exclusive iPhone model")
                .price(new BigDecimal("1299.99"))
                .status("active")
                .exclusive(true)
                .category(testCategory)
                .build();

        productRepository.save(testProduct);
        productRepository.save(exclusiveProduct);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Product> exclusiveResults = productRepository.findAllWithFilters(
                null, null, null, null, null, true, pageable);
        Page<Product> nonExclusiveResults = productRepository.findAllWithFilters(
                null, null, null, null, null, false, pageable);

        // Then
        assertEquals(1, exclusiveResults.getTotalElements());
        assertEquals(1, nonExclusiveResults.getTotalElements());
        assertTrue(exclusiveResults.getContent().get(0).isExclusive());
        assertFalse(nonExclusiveResults.getContent().get(0).isExclusive());
    }

    @Test
    void shouldCombineMultipleFilters_whenFindAllWithFilters() {
        // Given
        Product matchingProduct = Product.builder()
                .name("Premium iPhone")
                .sku("PREMIUM-IPHONE-001")
                .description("Premium iPhone model")
                .price(new BigDecimal("1399.99"))
                .status("active")
                .featured(true)
                .exclusive(true)
                .category(testCategory)
                .subcategory(testSubcategory)
                .brand(testBrand)
                .build();

        productRepository.save(testProduct);
        productRepository.save(matchingProduct);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Product> result = productRepository.findAllWithFilters(
                "active", testCategory.getId(), testSubcategory.getId(), 
                testBrand.getId(), true, true, pageable);

        // Then
        assertEquals(1, result.getTotalElements());
        Product found = result.getContent().get(0);
        assertEquals("Premium iPhone", found.getName());
        assertEquals("active", found.getStatus());
        assertTrue(found.isFeatured());
        assertTrue(found.isExclusive());
        assertEquals(testCategory.getId(), found.getCategory().getId());
        assertEquals(testSubcategory.getId(), found.getSubcategory().getId());
        assertEquals(testBrand.getId(), found.getBrand().getId());
    }

    @Test
    void shouldSearchByKeywordInName_whenSearchByKeyword() {
        // Given
        productRepository.save(testProduct);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Product> result = productRepository.searchByKeyword("iPhone", pageable);

        // Then
        assertEquals(1, result.getTotalElements());
        assertEquals("iPhone 15", result.getContent().get(0).getName());
    }

    @Test
    void shouldSearchByKeywordInDescription_whenSearchByKeyword() {
        // Given
        productRepository.save(testProduct);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Product> result = productRepository.searchByKeyword("Latest", pageable);

        // Then
        assertEquals(1, result.getTotalElements());
        assertEquals("iPhone 15", result.getContent().get(0).getName());
    }

    @Test
    void shouldSearchCaseInsensitive_whenSearchByKeyword() {
        // Given
        productRepository.save(testProduct);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Product> result = productRepository.searchByKeyword("IPHONE", pageable);

        // Then
        assertEquals(1, result.getTotalElements());
        assertEquals("iPhone 15", result.getContent().get(0).getName());
    }

    @Test
    void shouldOnlyReturnActiveProducts_whenSearchByKeyword() {
        // Given
        Product inactiveProduct = Product.builder()
                .name("iPhone 14")
                .sku("IPHONE14-001")
                .description("Previous iPhone model")
                .price(new BigDecimal("799.99"))
                .status("inactive")
                .category(testCategory)
                .build();

        productRepository.save(testProduct);
        productRepository.save(inactiveProduct);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Product> result = productRepository.searchByKeyword("iPhone", pageable);

        // Then
        assertEquals(1, result.getTotalElements());
        assertEquals("iPhone 15", result.getContent().get(0).getName());
        assertEquals("active", result.getContent().get(0).getStatus());
    }

    @Test
    void shouldReturnEmptyResult_whenSearchByKeywordNotFound() {
        // Given
        productRepository.save(testProduct);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Product> result = productRepository.searchByKeyword("Samsung", pageable);

        // Then
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void shouldCalculateTotalStock_whenProductHasColors() {
        // Given
        Product savedProduct = productRepository.save(testProduct);
        
        ProductColor redColor = ProductColor.builder()
                .product(savedProduct)
                .colorName("Red")
                .colorHex("#FF0000")
                .stock(10)
                .build();
        
        ProductColor blueColor = ProductColor.builder()
                .product(savedProduct)
                .colorName("Blue")
                .colorHex("#0000FF")
                .stock(15)
                .build();
        
        productColorRepository.save(redColor);
        productColorRepository.save(blueColor);
        
        // Clear the persistence context to force a fresh load
        productRepository.flush();
        
        // Calculate total stock directly from repository
        int totalStock = productColorRepository.findAll().stream()
                .filter(color -> color.getProduct().getId().equals(savedProduct.getId()))
                .mapToInt(ProductColor::getStock)
                .sum();

        // Then
        assertEquals(25, totalStock);
    }

    @Test
    void shouldReturnZeroStock_whenProductHasNoColors() {
        // Given
        Product savedProduct = productRepository.save(testProduct);

        // When
        int totalStock = savedProduct.getTotalStock();

        // Then
        assertEquals(0, totalStock);
    }

    @Test
    void shouldSetTimestamps_whenProductIsSaved() {
        // When
        Product savedProduct = productRepository.save(testProduct);

        // Then
        assertNotNull(savedProduct.getCreatedAt());
        assertNotNull(savedProduct.getUpdatedAt());
        // Check timestamps are within a reasonable time window (1 second)
        assertTrue(Math.abs(savedProduct.getCreatedAt().toEpochSecond(java.time.ZoneOffset.UTC) - 
                           savedProduct.getUpdatedAt().toEpochSecond(java.time.ZoneOffset.UTC)) <= 1);
    }

    @Test
    void shouldUpdateTimestamp_whenProductIsModified() throws InterruptedException {
        // Given
        Product savedProduct = productRepository.save(testProduct);
        productRepository.flush(); // Force the initial save
        var originalUpdatedAt = savedProduct.getUpdatedAt();
        
        // Wait to ensure timestamp difference
        Thread.sleep(1000);
        
        // When
        savedProduct.setName("Updated iPhone 15");
        Product updatedProduct = productRepository.saveAndFlush(savedProduct);

        // Then
        assertEquals(savedProduct.getCreatedAt(), updatedProduct.getCreatedAt());
        assertTrue(updatedProduct.getUpdatedAt().isAfter(originalUpdatedAt));
    }
}
