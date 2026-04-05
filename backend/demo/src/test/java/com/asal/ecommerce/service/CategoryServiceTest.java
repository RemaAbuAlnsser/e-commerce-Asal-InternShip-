package com.asal.ecommerce.service;

import com.asal.ecommerce.dto.CategoryCreateRequest;
import com.asal.ecommerce.dto.CategoryResponse;
import com.asal.ecommerce.dto.CategoryUpdateRequest;
import com.asal.ecommerce.mapper.CategoryMapper;
import com.asal.ecommerce.model.Category;
import com.asal.ecommerce.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CategoryServiceTest {

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private CategoryMapper categoryMapper;

    @InjectMocks
    private CategoryService categoryService;

    private Category testCategory;
    private CategoryCreateRequest createRequest;
    private CategoryUpdateRequest updateRequest;
    private CategoryResponse categoryResponse;

    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Electronics");
        testCategory.setSlug("electronics");
        testCategory.setDescription("Electronic devices");
        testCategory.setIsActive(true);
        testCategory.setSubcategories(new ArrayList<>());

        createRequest = new CategoryCreateRequest();
        createRequest.setName("Electronics");
        createRequest.setDescription("Electronic devices");

        updateRequest = new CategoryUpdateRequest();
        updateRequest.setName("Updated Electronics");
        updateRequest.setDescription("Updated description");

        categoryResponse = new CategoryResponse();
        categoryResponse.setId(1L);
        categoryResponse.setName("Electronics");
        categoryResponse.setSlug("electronics");
        categoryResponse.setDescription("Electronic devices");
        categoryResponse.setIsActive(true);
    }

    @Test
    void shouldCreateCategory_whenValidRequest() {
        // Given
        when(categoryMapper.toEntity(createRequest)).thenReturn(testCategory);
        when(categoryRepository.existsByNameIgnoreCase("Electronics")).thenReturn(false);
        when(categoryRepository.existsBySlugIgnoreCase("electronics")).thenReturn(false);
        when(categoryRepository.save(testCategory)).thenReturn(testCategory);
        when(categoryMapper.toResponse(testCategory)).thenReturn(categoryResponse);

        // When
        CategoryResponse result = categoryService.createCategory(createRequest);

        // Then
        assertNotNull(result);
        assertEquals(categoryResponse.getName(), result.getName());
        verify(categoryRepository).existsByNameIgnoreCase("Electronics");
        verify(categoryRepository).existsBySlugIgnoreCase("electronics");
        verify(categoryRepository).save(testCategory);
    }

    @Test
    void shouldThrowException_whenCreateCategoryWithDuplicateName() {
        // Given
        when(categoryMapper.toEntity(createRequest)).thenReturn(testCategory);
        when(categoryRepository.existsByNameIgnoreCase("Electronics")).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> categoryService.createCategory(createRequest));
        assertEquals("Category with name 'Electronics' already exists", exception.getMessage());
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void shouldThrowException_whenCreateCategoryWithDuplicateSlug() {
        // Given
        when(categoryMapper.toEntity(createRequest)).thenReturn(testCategory);
        when(categoryRepository.existsByNameIgnoreCase("Electronics")).thenReturn(false);
        when(categoryRepository.existsBySlugIgnoreCase("electronics")).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> categoryService.createCategory(createRequest));
        assertEquals("Category with slug 'electronics' already exists", exception.getMessage());
        verify(categoryRepository, never()).save(any());
    }

    @Test
    void shouldUpdateCategory_whenValidRequest() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(categoryMapper.generateSlug(null, "Updated Electronics")).thenReturn("updated-electronics");
        when(categoryRepository.existsByNameIgnoreCaseAndIdNot("Updated Electronics", 1L)).thenReturn(false);
        when(categoryRepository.existsBySlugIgnoreCaseAndIdNot("updated-electronics", 1L)).thenReturn(false);
        when(categoryRepository.save(testCategory)).thenReturn(testCategory);
        when(categoryMapper.toResponse(testCategory)).thenReturn(categoryResponse);

        // When
        CategoryResponse result = categoryService.updateCategory(1L, updateRequest);

        // Then
        assertNotNull(result);
        verify(categoryMapper).updateEntity(testCategory, updateRequest);
        verify(categoryRepository).save(testCategory);
    }

    @Test
    void shouldThrowException_whenUpdateCategoryNotFound() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> categoryService.updateCategory(1L, updateRequest));
        assertEquals("Category not found with id: 1", exception.getMessage());
    }

    @Test
    void shouldReturnCategory_whenGetByIdFound() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(categoryMapper.toResponse(testCategory)).thenReturn(categoryResponse);

        // When
        CategoryResponse result = categoryService.getCategoryById(1L);

        // Then
        assertNotNull(result);
        assertEquals(categoryResponse.getId(), result.getId());
        assertEquals(categoryResponse.getName(), result.getName());
    }

    @Test
    void shouldThrowException_whenGetByIdNotFound() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> categoryService.getCategoryById(1L));
        assertEquals("Category not found with id: 1", exception.getMessage());
    }

    @Test
    void shouldReturnPaginatedResult_whenGetAllCategories() {
        // Given
        List<Category> categories = Arrays.asList(testCategory);
        Page<Category> categoryPage = new PageImpl<>(categories);
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"));
        
        when(categoryRepository.findAll(pageable)).thenReturn(categoryPage);
        when(categoryMapper.toResponse(testCategory)).thenReturn(categoryResponse);

        // When
        Page<CategoryResponse> result = categoryService.getAllCategories(0, 10, "name", "asc");

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(categoryResponse.getName(), result.getContent().get(0).getName());
    }

    @Test
    void shouldReturnSearchResults_whenSearchCategories() {
        // Given
        List<Category> categories = Arrays.asList(testCategory);
        Page<Category> categoryPage = new PageImpl<>(categories);
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"));
        
        when(categoryRepository.findByNameContainingIgnoreCase("Electronics", pageable)).thenReturn(categoryPage);
        when(categoryMapper.toResponse(testCategory)).thenReturn(categoryResponse);

        // When
        Page<CategoryResponse> result = categoryService.searchCategories("Electronics", 0, 10, "name", "asc");

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(categoryResponse.getName(), result.getContent().get(0).getName());
    }

    @Test
    void shouldReturnEmptyPage_whenSearchCategoriesNoResults() {
        // Given
        Page<Category> emptyPage = new PageImpl<>(new ArrayList<>());
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"));
        
        when(categoryRepository.findByNameContainingIgnoreCase("NonExistent", pageable)).thenReturn(emptyPage);

        // When
        Page<CategoryResponse> result = categoryService.searchCategories("NonExistent", 0, 10, "name", "asc");

        // Then
        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertTrue(result.getContent().isEmpty());
    }

    @Test
    void shouldDeleteCategory_whenCategoryExists() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));

        // When
        categoryService.deleteCategory(1L);

        // Then
        verify(categoryRepository).delete(testCategory);
    }

    @Test
    void shouldThrowException_whenDeleteCategoryNotFound() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> categoryService.deleteCategory(1L));
        assertEquals("Category not found with id: 1", exception.getMessage());
        verify(categoryRepository, never()).delete(any());
    }

    @Test
    void shouldThrowException_whenDeleteCategoryWithSubcategories() {
        // Given
        testCategory.getSubcategories().add(new com.asal.ecommerce.model.Subcategory());
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> categoryService.deleteCategory(1L));
        assertEquals("Cannot delete category with existing subcategories", exception.getMessage());
        verify(categoryRepository, never()).delete(any());
    }

    @Test
    void shouldActivateCategory_whenToggleStatusToTrue() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.save(testCategory)).thenReturn(testCategory);
        when(categoryMapper.toResponse(testCategory)).thenReturn(categoryResponse);

        // When
        CategoryResponse result = categoryService.updateCategoryStatus(1L, true);

        // Then
        assertNotNull(result);
        assertTrue(testCategory.getIsActive());
        verify(categoryRepository).save(testCategory);
    }

    @Test
    void shouldDeactivateCategory_whenToggleStatusToFalse() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(categoryRepository.save(testCategory)).thenReturn(testCategory);
        when(categoryMapper.toResponse(testCategory)).thenReturn(categoryResponse);

        // When
        CategoryResponse result = categoryService.updateCategoryStatus(1L, false);

        // Then
        assertNotNull(result);
        assertFalse(testCategory.getIsActive());
        verify(categoryRepository).save(testCategory);
    }

    @Test
    void shouldReturnActiveCategories_whenGetActiveCategories() {
        // Given
        List<Category> categories = Arrays.asList(testCategory);
        Page<Category> categoryPage = new PageImpl<>(categories);
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"));
        
        when(categoryRepository.findByIsActiveTrue(pageable)).thenReturn(categoryPage);
        when(categoryMapper.toResponse(testCategory)).thenReturn(categoryResponse);

        // When
        Page<CategoryResponse> result = categoryService.getActiveCategories(0, 10, "name", "asc");

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(categoryResponse.getName(), result.getContent().get(0).getName());
    }

    @Test
    void shouldReturnActiveCategory_whenGetActiveCategoryBySlug() {
        // Given
        when(categoryRepository.findBySlugAndIsActiveTrue("electronics")).thenReturn(Optional.of(testCategory));
        when(categoryMapper.toResponse(testCategory)).thenReturn(categoryResponse);

        // When
        CategoryResponse result = categoryService.getActiveCategoryBySlug("electronics");

        // Then
        assertNotNull(result);
        assertEquals(categoryResponse.getSlug(), result.getSlug());
    }

    @Test
    void shouldThrowException_whenGetActiveCategoryBySlugNotFound() {
        // Given
        when(categoryRepository.findBySlugAndIsActiveTrue("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> categoryService.getActiveCategoryBySlug("nonexistent"));
        assertEquals("Active category not found with slug: nonexistent", exception.getMessage());
    }
}
