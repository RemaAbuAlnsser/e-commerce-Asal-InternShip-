// package com.asal.ecommerce.repository;

// import com.asal.ecommerce.model.*;
// import org.junit.jupiter.api.*;
// import org.springframework.beans.factory.annotation.Autowired;
// import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
// import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
// import org.springframework.data.domain.*;

// import java.math.BigDecimal;
// import java.util.*;

// import static org.assertj.core.api.Assertions.*;

// @DataJpaTest
// @DisplayName("ProductRepository Query Tests")
// class ProductRepositoryTest {

//     @Autowired
//     private TestEntityManager em;

//     @Autowired
//     private ProductRepository productRepo;

//     // ── Test data ──────────────────────────────────────────────────────────────
//     private Category    electronics;
//     private Category    fashion;
//     private Subcategory phones;
//     private Brand       samsung;
//     private Product     activeProduct;
//     private Product     inactiveProduct;
//     private Product     featuredProduct;

//     @BeforeEach
//     void setUp() {
//         // Categories
//         electronics = new Category();
//         electronics.setName("Electronics");
//         em.persist(electronics);

//         fashion = new Category();
//         fashion.setName("Fashion");
//         em.persist(fashion);

//         // Subcategory
//         phones = new Subcategory();
//         phones.setName("Phones");
//         phones.setCategory(electronics);
//         em.persist(phones);

//         // Brand
//         samsung = new Brand();
//         samsung.setName("Samsung");
//         em.persist(samsung);

//         // Active product
//         activeProduct = buildProduct("Galaxy S24", "SAMS-001", electronics, phones, samsung, "active", false, false, 100.00);
//         em.persist(activeProduct);

//         // Inactive product
//         inactiveProduct = buildProduct("Old Model", "OLD-001", electronics, null, samsung, "inactive", false, false, 50.00);
//         em.persist(inactiveProduct);

//         // Featured product
//         featuredProduct = buildProduct("Premium Shirt", "SHIRT-001", fashion, null, null, "active", true, false, 200.00);
//         em.persist(featuredProduct);

//         em.flush();
//     }

//     // =========================================================================
//     // findBySku
//     // =========================================================================

//     @Nested
//     @DisplayName("findBySku()")
//     class FindBySkuTests {

//         @Test
//         @DisplayName("should find product by exact SKU")
//         void findBySku_existingSku_returnsProduct() {
//             Optional<Product> result = productRepo.findBySku("SAMS-001");

//             assertThat(result).isPresent();
//             assertThat(result.get().getName()).isEqualTo("Galaxy S24");
//         }

//         @Test
//         @DisplayName("should return empty for non-existing SKU")
//         void findBySku_nonExistingSku_returnsEmpty() {
//             Optional<Product> result = productRepo.findBySku("NOEXIST-999");

//             assertThat(result).isEmpty();
//         }
//     }

//     // =========================================================================
//     // existsBySku
//     // =========================================================================

//     @Nested
//     @DisplayName("existsBySku()")
//     class ExistsBySkuTests {

//         @Test
//         @DisplayName("should return true for existing SKU")
//         void existsBySku_existingSku_returnsTrue() {
//             assertThat(productRepo.existsBySku("SAMS-001")).isTrue();
//         }

//         @Test
//         @DisplayName("should return false for non-existing SKU")
//         void existsBySku_nonExistingSku_returnsFalse() {
//             assertThat(productRepo.existsBySku("FAKE-SKU")).isFalse();
//         }
//     }

//     // =========================================================================
//     // findAllWithFilters
//     // =========================================================================

//     @Nested
//     @DisplayName("findAllWithFilters()")
//     class FindAllWithFiltersTests {

//         @Test
//         @DisplayName("should return only active products when status=active")
//         void findAllWithFilters_statusActive_returnsOnlyActive() {
//             Pageable pageable = PageRequest.of(0, 10);

//             Page<Product> result = productRepo.findAllWithFilters(
//                     "active", null, null, null, null, null, pageable
//             );

//             assertThat(result.getContent()).hasSize(2); // activeProduct + featuredProduct
//             assertThat(result.getContent())
//                     .extracting(Product::getStatus)
//                     .containsOnly("active");
//         }

//         @Test
//         @DisplayName("should return only inactive products when status=inactive")
//         void findAllWithFilters_statusInactive_returnsOnlyInactive() {
//             Pageable pageable = PageRequest.of(0, 10);

//             Page<Product> result = productRepo.findAllWithFilters(
//                     "inactive", null, null, null, null, null, pageable
//             );

//             assertThat(result.getContent()).hasSize(1);
//             assertThat(result.getContent().get(0).getName()).isEqualTo("Old Model");
//         }

//         @Test
//         @DisplayName("should filter by categoryId correctly")
//         void findAllWithFilters_byCategoryId_returnsMatchingProducts() {
//             Pageable pageable = PageRequest.of(0, 10);

//             Page<Product> result = productRepo.findAllWithFilters(
//                     null, electronics.getId(), null, null, null, null, pageable
//             );

//             assertThat(result.getContent()).hasSize(2); // activeProduct + inactiveProduct
//             assertThat(result.getContent())
//                     .extracting(p -> p.getCategory().getName())
//                     .containsOnly("Electronics");
//         }

//         @Test
//         @DisplayName("should filter by subcategoryId correctly")
//         void findAllWithFilters_bySubcategoryId_returnsMatchingProducts() {
//             Pageable pageable = PageRequest.of(0, 10);

//             Page<Product> result = productRepo.findAllWithFilters(
//                     null, null, phones.getId(), null, null, null, pageable
//             );

//             assertThat(result.getContent()).hasSize(1);
//             assertThat(result.getContent().get(0).getName()).isEqualTo("Galaxy S24");
//         }

//         @Test
//         @DisplayName("should filter by brandId correctly")
//         void findAllWithFilters_byBrandId_returnsMatchingProducts() {
//             Pageable pageable = PageRequest.of(0, 10);

//             Page<Product> result = productRepo.findAllWithFilters(
//                     null, null, null, samsung.getId(), null, null, pageable
//             );

//             assertThat(result.getContent()).hasSize(2); // Galaxy + Old Model
//         }

//         @Test
//         @DisplayName("should filter featured products when isFeatured=true")
//         void findAllWithFilters_featuredTrue_returnsOnlyFeatured() {
//             Pageable pageable = PageRequest.of(0, 10);

//             Page<Product> result = productRepo.findAllWithFilters(
//                     null, null, null, null, true, null, pageable
//             );

//             assertThat(result.getContent()).hasSize(1);
//             assertThat(result.getContent().get(0).getName()).isEqualTo("Premium Shirt");
//         }

//         @Test
//         @DisplayName("should return all products when all filters are null")
//         void findAllWithFilters_noFilters_returnsAll() {
//             Pageable pageable = PageRequest.of(0, 10);

//             Page<Product> result = productRepo.findAllWithFilters(
//                     null, null, null, null, null, null, pageable
//             );

//             assertThat(result.getContent()).hasSize(3);
//         }

//         @Test
//         @DisplayName("should combine multiple filters correctly")
//         void findAllWithFilters_combinedFilters_returnsMatchingProducts() {
//             Pageable pageable = PageRequest.of(0, 10);

//             Page<Product> result = productRepo.findAllWithFilters(
//                     "active", electronics.getId(), null, null, null, null, pageable
//             );

//             assertThat(result.getContent()).hasSize(1);
//             assertThat(result.getContent().get(0).getName()).isEqualTo("Galaxy S24");
//         }

//         @Test
//         @DisplayName("should return correct pagination metadata")
//         void findAllWithFilters_pagination_returnsCorrectMetadata() {
//             Pageable pageable = PageRequest.of(0, 2);

//             Page<Product> result = productRepo.findAllWithFilters(
//                     null, null, null, null, null, null, pageable
//             );

//             assertThat(result.getTotalElements()).isEqualTo(3);
//             assertThat(result.getTotalPages()).isEqualTo(2);
//             assertThat(result.getContent()).hasSize(2);
//         }

//         @Test
//         @DisplayName("should return empty when no products match filters")
//         void findAllWithFilters_noMatch_returnsEmptyPage() {
//             Pageable pageable = PageRequest.of(0, 10);

//             Page<Product> result = productRepo.findAllWithFilters(
//                     "active", fashion.getId(), phones.getId(), null, null, null, pageable
//             );

//             // fashion category has no product with phones subcategory
//             assertThat(result.getContent()).isEmpty();
//         }
//     }

//     // =========================================================================
//     // searchByKeyword
//     // =========================================================================

//     @Nested
//     @DisplayName("searchByKeyword()")
//     class SearchByKeywordTests {

//         @Test
//         @DisplayName("should find product by name keyword (case-insensitive)")
//         void searchByKeyword_nameMatch_returnsProducts() {
//             Pageable pageable = PageRequest.of(0, 10);

//             Page<Product> result = productRepo.searchByKeyword("galaxy", pageable);

//             assertThat(result.getContent()).hasSize(1);
//             assertThat(result.getContent().get(0).getName()).isEqualTo("Galaxy S24");
//         }

//         @Test
//         @DisplayName("should find by partial name match")
//         void searchByKeyword_partialName_returnsProducts() {
//             Pageable pageable = PageRequest.of(0, 10);

//             Page<Product> result = productRepo.searchByKeyword("shirt", pageable);

//             assertThat(result.getContent()).hasSize(1);
//             assertThat(result.getContent().get(0).getName()).isEqualTo("Premium Shirt");
//         }

//         @Test
//         @DisplayName("should NOT return inactive products in search")
//         void searchByKeyword_inactiveProduct_notIncluded() {
//             Pageable pageable = PageRequest.of(0, 10);

//             Page<Product> result = productRepo.searchByKeyword("old", pageable);

//             // "Old Model" is inactive — should not appear
//             assertThat(result.getContent()).isEmpty();
//         }

//         @Test
//         @DisplayName("should return empty page for no-match keyword")
//         void searchByKeyword_noMatch_returnsEmpty() {
//             Pageable pageable = PageRequest.of(0, 10);

//             Page<Product> result = productRepo.searchByKeyword("notexistingatall", pageable);

//             assertThat(result.getContent()).isEmpty();
//         }

//         @Test
//         @DisplayName("should be case-insensitive")
//         void searchByKeyword_differentCase_returnsProduct() {
//             Pageable pageable = PageRequest.of(0, 10);

//             Page<Product> upper = productRepo.searchByKeyword("GALAXY", pageable);
//             Page<Product> lower = productRepo.searchByKeyword("galaxy", pageable);
//             Page<Product> mixed = productRepo.searchByKeyword("GaLaXy", pageable);

//             assertThat(upper.getTotalElements()).isEqualTo(1);
//             assertThat(lower.getTotalElements()).isEqualTo(1);
//             assertThat(mixed.getTotalElements()).isEqualTo(1);
//         }
//     }

//     // =========================================================================
//     // HELPER
//     // =========================================================================

//     private Product buildProduct(
//             String name, String sku,
//             Category category, Subcategory subcategory, Brand brand,
//             String status, boolean featured, boolean exclusive,
//             double price
//     ) {
//         Product p = new Product();
//         p.setName(name);
//         p.setSku(sku);
//         p.setDescription("Description for " + name);
//         p.setPrice(BigDecimal.valueOf(price));
//         p.setStatus(status);
//         p.setFeatured(featured);
//         p.setExclusive(exclusive);
//         p.setCategory(category);
//         p.setSubcategory(subcategory);
//         p.setBrand(brand);
//         p.setColors(new ArrayList<>());
//         return p;
//     }
// }