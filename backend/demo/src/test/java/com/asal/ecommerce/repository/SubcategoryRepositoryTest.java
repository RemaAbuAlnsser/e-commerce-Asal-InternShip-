package com.asal.ecommerce.repository;

import com.asal.ecommerce.model.Category;
import com.asal.ecommerce.model.Subcategory;
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

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class SubcategoryRepositoryTest {

    @Autowired
    private SubcategoryRepository subcategoryRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private ProductRepository productRepository;

    private Category testCategory;
    private Subcategory testSubcategory;

    @BeforeEach
    void setUp() {
        // Clean up database in correct order to avoid FK violations
        productRepository.deleteAll();
        subcategoryRepository.deleteAll();
        categoryRepository.deleteAll();
        
        testCategory = new Category();
        testCategory.setName("Electronics");
        testCategory.setSlug("electronics");
        testCategory.setDescription("Electronic devices");
        testCategory.setIsActive(true);
        testCategory = categoryRepository.save(testCategory);

        testSubcategory = new Subcategory();
        testSubcategory.setName("Smartphones");
        testSubcategory.setSlug("smartphones");
        testSubcategory.setDescription("Mobile phones");
        testSubcategory.setIsActive(true);
        testSubcategory.setCategory(testCategory);
    }

    @Test
    void shouldReturnPaginatedResults_whenFindByCategoryId() {
        // Given
        subcategoryRepository.save(testSubcategory);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Subcategory> result = subcategoryRepository.findByCategoryId(testCategory.getId(), pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Smartphones", result.getContent().get(0).getName());
        assertEquals(testCategory.getId(), result.getContent().get(0).getCategory().getId());
    }

    @Test
    void shouldReturnTrue_whenExistsByNameIgnoreCaseAndCategoryId() {
        // Given
        subcategoryRepository.save(testSubcategory);

        // When
        boolean exists = subcategoryRepository.existsByNameIgnoreCaseAndCategoryId("SMARTPHONES", testCategory.getId());

        // Then
        assertTrue(exists);
    }

    @Test
    void shouldReturnFalse_whenNotExistsByNameIgnoreCaseAndCategoryId() {
        // When
        boolean exists = subcategoryRepository.existsByNameIgnoreCaseAndCategoryId("NonExistent", testCategory.getId());

        // Then
        assertFalse(exists);
    }

    @Test
    void shouldReturnTrue_whenExistsBySlugIgnoreCaseAndCategoryId() {
        // Given
        subcategoryRepository.save(testSubcategory);

        // When
        boolean exists = subcategoryRepository.existsBySlugIgnoreCaseAndCategoryId("SMARTPHONES", testCategory.getId());

        // Then
        assertTrue(exists);
    }

    @Test
    void shouldReturnFalse_whenNotExistsBySlugIgnoreCaseAndCategoryId() {
        // When
        boolean exists = subcategoryRepository.existsBySlugIgnoreCaseAndCategoryId("non-existent", testCategory.getId());

        // Then
        assertFalse(exists);
    }

    @Test
    void shouldFindByNameContainingIgnoreCase() {
        // Given
        subcategoryRepository.save(testSubcategory);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Subcategory> result = subcategoryRepository.findByNameContainingIgnoreCase("smart", pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Smartphones", result.getContent().get(0).getName());
    }

    @Test
    void shouldFindByCategoryIdAndNameContainingIgnoreCase() {
        // Given
        subcategoryRepository.save(testSubcategory);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Subcategory> result = subcategoryRepository.findByCategoryIdAndNameContainingIgnoreCase(
            testCategory.getId(), "smart", pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Smartphones", result.getContent().get(0).getName());
        assertEquals(testCategory.getId(), result.getContent().get(0).getCategory().getId());
    }

    @Test
    void shouldReturnOnlyActiveSubcategories_whenFindByIsActiveTrue() {
        // Given
        Subcategory activeSubcategory = new Subcategory();
        activeSubcategory.setName("Active Subcategory");
        activeSubcategory.setSlug("active-subcategory");
        activeSubcategory.setIsActive(true);
        activeSubcategory.setCategory(testCategory);

        Subcategory inactiveSubcategory = new Subcategory();
        inactiveSubcategory.setName("Inactive Subcategory");
        inactiveSubcategory.setSlug("inactive-subcategory");
        inactiveSubcategory.setIsActive(false);
        inactiveSubcategory.setCategory(testCategory);

        subcategoryRepository.save(activeSubcategory);
        subcategoryRepository.save(inactiveSubcategory);

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Subcategory> result = subcategoryRepository.findByIsActiveTrue(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().get(0).getIsActive());
        assertEquals("Active Subcategory", result.getContent().get(0).getName());
    }

    @Test
    void shouldFindActiveSubcategory_whenFindBySlugAndIsActiveTrue() {
        // Given
        subcategoryRepository.save(testSubcategory);

        // When
        Optional<Subcategory> result = subcategoryRepository.findBySlugAndIsActiveTrue("smartphones");

        // Then
        assertTrue(result.isPresent());
        assertEquals("Smartphones", result.get().getName());
        assertTrue(result.get().getIsActive());
    }

    @Test
    void shouldNotFindInactiveSubcategory_whenFindBySlugAndIsActiveTrue() {
        // Given
        testSubcategory.setIsActive(false);
        subcategoryRepository.save(testSubcategory);

        // When
        Optional<Subcategory> result = subcategoryRepository.findBySlugAndIsActiveTrue("smartphones");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void shouldFindByCategorySlugAndIsActiveTrue() {
        // Given
        subcategoryRepository.save(testSubcategory);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Subcategory> result = subcategoryRepository.findByCategorySlugAndIsActiveTrue("electronics", pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Smartphones", result.getContent().get(0).getName());
        assertTrue(result.getContent().get(0).getIsActive());
        assertEquals("electronics", result.getContent().get(0).getCategory().getSlug());
    }

    @Test
    void shouldFilterByActiveStatus_whenFindByIsActive() {
        // Given
        Subcategory activeSubcategory = new Subcategory();
        activeSubcategory.setName("Active Subcategory");
        activeSubcategory.setSlug("active-subcategory");
        activeSubcategory.setIsActive(true);
        activeSubcategory.setCategory(testCategory);

        Subcategory inactiveSubcategory = new Subcategory();
        inactiveSubcategory.setName("Inactive Subcategory");
        inactiveSubcategory.setSlug("inactive-subcategory");
        inactiveSubcategory.setIsActive(false);
        inactiveSubcategory.setCategory(testCategory);

        subcategoryRepository.save(activeSubcategory);
        subcategoryRepository.save(inactiveSubcategory);

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Subcategory> activeResults = subcategoryRepository.findByIsActive(true, pageable);
        Page<Subcategory> inactiveResults = subcategoryRepository.findByIsActive(false, pageable);

        // Then
        assertEquals(1, activeResults.getTotalElements());
        assertEquals(1, inactiveResults.getTotalElements());
        assertTrue(activeResults.getContent().get(0).getIsActive());
        assertFalse(inactiveResults.getContent().get(0).getIsActive());
    }

    @Test
    void shouldFindBySlugAndCategoryId() {
        // Given
        subcategoryRepository.save(testSubcategory);

        // When
        Optional<Subcategory> result = subcategoryRepository.findBySlugAndCategoryId("smartphones", testCategory.getId());

        // Then
        assertTrue(result.isPresent());
        assertEquals("Smartphones", result.get().getName());
        assertEquals(testCategory.getId(), result.get().getCategory().getId());
    }

    @Test
    void shouldNotFindBySlugAndCategoryId_whenWrongCategory() {
        // Given
        subcategoryRepository.save(testSubcategory);

        // When
        Optional<Subcategory> result = subcategoryRepository.findBySlugAndCategoryId("smartphones", 999L);

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void shouldFindByCategoryIdAndIsActive() {
        // Given
        Subcategory activeSubcategory = new Subcategory();
        activeSubcategory.setName("Active Subcategory");
        activeSubcategory.setSlug("active-subcategory");
        activeSubcategory.setIsActive(true);
        activeSubcategory.setCategory(testCategory);

        Subcategory inactiveSubcategory = new Subcategory();
        inactiveSubcategory.setName("Inactive Subcategory");
        inactiveSubcategory.setSlug("inactive-subcategory");
        inactiveSubcategory.setIsActive(false);
        inactiveSubcategory.setCategory(testCategory);

        subcategoryRepository.save(activeSubcategory);
        subcategoryRepository.save(inactiveSubcategory);

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Subcategory> activeResults = subcategoryRepository.findByCategoryIdAndIsActive(testCategory.getId(), true, pageable);
        Page<Subcategory> inactiveResults = subcategoryRepository.findByCategoryIdAndIsActive(testCategory.getId(), false, pageable);

        // Then
        assertEquals(1, activeResults.getTotalElements());
        assertEquals(1, inactiveResults.getTotalElements());
        assertTrue(activeResults.getContent().get(0).getIsActive());
        assertFalse(inactiveResults.getContent().get(0).getIsActive());
        assertEquals(testCategory.getId(), activeResults.getContent().get(0).getCategory().getId());
        assertEquals(testCategory.getId(), inactiveResults.getContent().get(0).getCategory().getId());
    }
}
