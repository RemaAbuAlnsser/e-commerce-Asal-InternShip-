package com.asal.ecommerce.repository;

import com.asal.ecommerce.model.Category;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
@Transactional
class CategoryRepositoryTest {

    @Autowired
    private CategoryRepository categoryRepository;

    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setName("Electronics");
        testCategory.setSlug("electronics");
        testCategory.setDescription("Electronic devices");
        testCategory.setIsActive(true);
    }

    @Test
    void shouldReturnTrue_whenExistsByNameIgnoreCase() {
        // Given
        categoryRepository.save(testCategory);

        // When
        boolean exists = categoryRepository.existsByNameIgnoreCase("ELECTRONICS");

        // Then
        assertTrue(exists);
    }

    @Test
    void shouldReturnFalse_whenNotExistsByNameIgnoreCase() {
        // When
        boolean exists = categoryRepository.existsByNameIgnoreCase("NonExistent");

        // Then
        assertFalse(exists);
    }

    @Test
    void shouldReturnTrue_whenExistsBySlugIgnoreCase() {
        // Given
        categoryRepository.save(testCategory);

        // When
        boolean exists = categoryRepository.existsBySlugIgnoreCase("ELECTRONICS");

        // Then
        assertTrue(exists);
    }

    @Test
    void shouldReturnFalse_whenNotExistsBySlugIgnoreCase() {
        // When
        boolean exists = categoryRepository.existsBySlugIgnoreCase("non-existent");

        // Then
        assertFalse(exists);
    }

    @Test
    void shouldFindByNameContainingIgnoreCase_withPageable() {
        // Given
        categoryRepository.save(testCategory);
        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Category> result = categoryRepository.findByNameContainingIgnoreCase("elect", pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Electronics", result.getContent().get(0).getName());
    }

    @Test
    void shouldReturnOnlyActiveCategories_whenFindByIsActiveTrue() {
        // Given
        Category activeCategory = new Category();
        activeCategory.setName("Active Category");
        activeCategory.setSlug("active-category");
        activeCategory.setIsActive(true);

        Category inactiveCategory = new Category();
        inactiveCategory.setName("Inactive Category");
        inactiveCategory.setSlug("inactive-category");
        inactiveCategory.setIsActive(false);

        categoryRepository.save(activeCategory);
        categoryRepository.save(inactiveCategory);

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Category> result = categoryRepository.findByIsActiveTrue(pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().get(0).getIsActive());
        assertEquals("Active Category", result.getContent().get(0).getName());
    }

    @Test
    void shouldFindCategory_whenFindBySlugAndIsActiveTrue() {
        // Given
        categoryRepository.save(testCategory);

        // When
        Optional<Category> result = categoryRepository.findBySlugAndIsActiveTrue("electronics");

        // Then
        assertTrue(result.isPresent());
        assertEquals("Electronics", result.get().getName());
        assertTrue(result.get().getIsActive());
    }

    @Test
    void shouldNotFindCategory_whenFindBySlugAndIsActiveTrueButInactive() {
        // Given
        testCategory.setIsActive(false);
        categoryRepository.save(testCategory);

        // When
        Optional<Category> result = categoryRepository.findBySlugAndIsActiveTrue("electronics");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void shouldNotFindCategory_whenFindBySlugAndIsActiveTrueButNotFound() {
        // When
        Optional<Category> result = categoryRepository.findBySlugAndIsActiveTrue("non-existent");

        // Then
        assertFalse(result.isPresent());
    }

    @Test
    void shouldFindBySlug() {
        // Given
        categoryRepository.save(testCategory);

        // When
        Optional<Category> result = categoryRepository.findBySlug("electronics");

        // Then
        assertTrue(result.isPresent());
        assertEquals("Electronics", result.get().getName());
    }

    @Test
    void shouldReturnFilteredResults_whenFindByIsActive() {
        // Given
        Category activeCategory = new Category();
        activeCategory.setName("Active Category");
        activeCategory.setSlug("active-category");
        activeCategory.setIsActive(true);

        Category inactiveCategory = new Category();
        inactiveCategory.setName("Inactive Category");
        inactiveCategory.setSlug("inactive-category");
        inactiveCategory.setIsActive(false);

        categoryRepository.save(activeCategory);
        categoryRepository.save(inactiveCategory);

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Category> activeResults = categoryRepository.findByIsActive(true, pageable);
        Page<Category> inactiveResults = categoryRepository.findByIsActive(false, pageable);

        // Then
        assertEquals(1, activeResults.getTotalElements());
        assertEquals(1, inactiveResults.getTotalElements());
        assertTrue(activeResults.getContent().get(0).getIsActive());
        assertFalse(inactiveResults.getContent().get(0).getIsActive());
    }

    @Test
    void shouldFindByIsActiveAndNameContainingIgnoreCase() {
        // Given
        Category activeElectronics = new Category();
        activeElectronics.setName("Electronics");
        activeElectronics.setSlug("electronics");
        activeElectronics.setIsActive(true);

        Category inactiveElectronics = new Category();
        inactiveElectronics.setName("Old Electronics");
        inactiveElectronics.setSlug("old-electronics");
        inactiveElectronics.setIsActive(false);

        categoryRepository.save(activeElectronics);
        categoryRepository.save(inactiveElectronics);

        Pageable pageable = PageRequest.of(0, 10);

        // When
        Page<Category> result = categoryRepository.findByIsActiveAndNameContainingIgnoreCase(true, "electronics", pageable);

        // Then
        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().get(0).getIsActive());
        assertEquals("Electronics", result.getContent().get(0).getName());
    }
}
