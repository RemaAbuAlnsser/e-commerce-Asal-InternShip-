package com.asal.ecommerce.service;

import com.asal.ecommerce.dto.SubcategoryCreateRequest;
import com.asal.ecommerce.dto.SubcategoryResponse;
import com.asal.ecommerce.dto.SubcategoryUpdateRequest;
import com.asal.ecommerce.mapper.SubcategoryMapper;
import com.asal.ecommerce.model.Category;
import com.asal.ecommerce.model.Subcategory;
import com.asal.ecommerce.repository.CategoryRepository;
import com.asal.ecommerce.repository.SubcategoryRepository;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubcategoryServiceTest {

    @Mock
    private SubcategoryRepository subcategoryRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private SubcategoryMapper subcategoryMapper;

    @InjectMocks
    private SubcategoryService subcategoryService;

    private Category testCategory;
    private Subcategory testSubcategory;
    private SubcategoryCreateRequest createRequest;
    private SubcategoryUpdateRequest updateRequest;
    private SubcategoryResponse subcategoryResponse;

    @BeforeEach
    void setUp() {
        testCategory = new Category();
        testCategory.setId(1L);
        testCategory.setName("Electronics");
        testCategory.setSlug("electronics");
        testCategory.setIsActive(true);

        testSubcategory = new Subcategory();
        testSubcategory.setId(1L);
        testSubcategory.setName("Smartphones");
        testSubcategory.setSlug("smartphones");
        testSubcategory.setDescription("Mobile phones");
        testSubcategory.setIsActive(true);
        testSubcategory.setCategory(testCategory);

        createRequest = new SubcategoryCreateRequest();
        createRequest.setName("Smartphones");
        createRequest.setDescription("Mobile phones");
        createRequest.setCategoryId(1L);

        updateRequest = new SubcategoryUpdateRequest();
        updateRequest.setName("Updated Smartphones");
        updateRequest.setDescription("Updated description");
        updateRequest.setCategoryId(1L);

        subcategoryResponse = new SubcategoryResponse();
        subcategoryResponse.setId(1L);
        subcategoryResponse.setName("Smartphones");
        subcategoryResponse.setSlug("smartphones");
        subcategoryResponse.setDescription("Mobile phones");
        subcategoryResponse.setIsActive(true);
        subcategoryResponse.setCategoryId(1L);
        subcategoryResponse.setCategoryName("Electronics");
    }

    @Test
    void shouldCreateSubcategory_whenValidRequest() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(subcategoryMapper.toEntity(createRequest, testCategory)).thenReturn(testSubcategory);
        when(subcategoryRepository.existsByNameIgnoreCaseAndCategoryId("Smartphones", 1L)).thenReturn(false);
        when(subcategoryRepository.existsBySlugIgnoreCaseAndCategoryId("smartphones", 1L)).thenReturn(false);
        when(subcategoryRepository.save(testSubcategory)).thenReturn(testSubcategory);
        when(subcategoryMapper.toResponse(testSubcategory)).thenReturn(subcategoryResponse);

        // When
        SubcategoryResponse result = subcategoryService.createSubcategory(createRequest);

        // Then
        assertNotNull(result);
        assertEquals(subcategoryResponse.getName(), result.getName());
        verify(categoryRepository).findById(1L);
        verify(subcategoryRepository).existsByNameIgnoreCaseAndCategoryId("Smartphones", 1L);
        verify(subcategoryRepository).existsBySlugIgnoreCaseAndCategoryId("smartphones", 1L);
        verify(subcategoryRepository).save(testSubcategory);
    }

    @Test
    void shouldThrowException_whenCreateSubcategoryWithNonExistentCategory() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> subcategoryService.createSubcategory(createRequest));
        assertEquals("Category not found with id: 1", exception.getMessage());
        verify(subcategoryRepository, never()).save(any());
    }

    @Test
    void shouldThrowException_whenCreateSubcategoryWithDuplicateNameInCategory() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(subcategoryMapper.toEntity(createRequest, testCategory)).thenReturn(testSubcategory);
        when(subcategoryRepository.existsByNameIgnoreCaseAndCategoryId("Smartphones", 1L)).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> subcategoryService.createSubcategory(createRequest));
        assertEquals("Subcategory with name 'Smartphones' already exists in this category", exception.getMessage());
        verify(subcategoryRepository, never()).save(any());
    }

    @Test
    void shouldThrowException_whenCreateSubcategoryWithDuplicateSlugInCategory() {
        // Given
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(subcategoryMapper.toEntity(createRequest, testCategory)).thenReturn(testSubcategory);
        when(subcategoryRepository.existsByNameIgnoreCaseAndCategoryId("Smartphones", 1L)).thenReturn(false);
        when(subcategoryRepository.existsBySlugIgnoreCaseAndCategoryId("smartphones", 1L)).thenReturn(true);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> subcategoryService.createSubcategory(createRequest));
        assertEquals("Subcategory with slug 'smartphones' already exists in this category", exception.getMessage());
        verify(subcategoryRepository, never()).save(any());
    }

    @Test
    void shouldUpdateSubcategory_whenValidRequest() {
        // Given
        when(subcategoryRepository.findById(1L)).thenReturn(Optional.of(testSubcategory));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(subcategoryMapper.generateSlug(null, "Updated Smartphones")).thenReturn("updated-smartphones");
        when(subcategoryRepository.existsByNameIgnoreCaseAndCategoryIdAndIdNot("Updated Smartphones", 1L, 1L)).thenReturn(false);
        when(subcategoryRepository.existsBySlugIgnoreCaseAndCategoryIdAndIdNot("updated-smartphones", 1L, 1L)).thenReturn(false);
        when(subcategoryRepository.save(testSubcategory)).thenReturn(testSubcategory);
        when(subcategoryMapper.toResponse(testSubcategory)).thenReturn(subcategoryResponse);

        // When
        SubcategoryResponse result = subcategoryService.updateSubcategory(1L, updateRequest);

        // Then
        assertNotNull(result);
        verify(subcategoryMapper).updateEntity(testSubcategory, updateRequest, testCategory);
        verify(subcategoryRepository).save(testSubcategory);
    }

    @Test
    void shouldThrowException_whenUpdateSubcategoryNotFound() {
        // Given
        when(subcategoryRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> subcategoryService.updateSubcategory(1L, updateRequest));
        assertEquals("Subcategory not found with id: 1", exception.getMessage());
    }

    @Test
    void shouldReturnSubcategory_whenGetByIdFound() {
        // Given
        when(subcategoryRepository.findById(1L)).thenReturn(Optional.of(testSubcategory));
        when(subcategoryMapper.toResponse(testSubcategory)).thenReturn(subcategoryResponse);

        // When
        SubcategoryResponse result = subcategoryService.getSubcategoryById(1L);

        // Then
        assertNotNull(result);
        assertEquals(subcategoryResponse.getId(), result.getId());
        assertEquals(subcategoryResponse.getName(), result.getName());
    }

    @Test
    void shouldThrowException_whenGetByIdNotFound() {
        // Given
        when(subcategoryRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> subcategoryService.getSubcategoryById(1L));
        assertEquals("Subcategory not found with id: 1", exception.getMessage());
    }

    @Test
    void shouldReturnPaginatedResult_whenGetSubcategoriesByCategory() {
        // Given
        List<Subcategory> subcategories = Arrays.asList(testSubcategory);
        Page<Subcategory> subcategoryPage = new PageImpl<>(subcategories);
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"));
        
        when(categoryRepository.existsById(1L)).thenReturn(true);
        when(subcategoryRepository.findByCategoryId(1L, pageable)).thenReturn(subcategoryPage);
        when(subcategoryMapper.toResponse(testSubcategory)).thenReturn(subcategoryResponse);

        // When
        Page<SubcategoryResponse> result = subcategoryService.getSubcategoriesByCategory(1L, 0, 10, "name", "asc");

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(subcategoryResponse.getName(), result.getContent().get(0).getName());
    }

    @Test
    void shouldThrowException_whenGetSubcategoriesByCategoryNotFound() {
        // Given
        when(categoryRepository.existsById(1L)).thenReturn(false);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> subcategoryService.getSubcategoriesByCategory(1L, 0, 10, "name", "asc"));
        assertEquals("Category not found with id: 1", exception.getMessage());
    }

    @Test
    void shouldReturnSearchResults_whenSearchSubcategories() {
        // Given
        List<Subcategory> subcategories = Arrays.asList(testSubcategory);
        Page<Subcategory> subcategoryPage = new PageImpl<>(subcategories);
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"));
        
        when(subcategoryRepository.findByNameContainingIgnoreCase("Smartphones", pageable)).thenReturn(subcategoryPage);
        when(subcategoryMapper.toResponse(testSubcategory)).thenReturn(subcategoryResponse);

        // When
        Page<SubcategoryResponse> result = subcategoryService.searchSubcategories("Smartphones", 0, 10, "name", "asc");

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(subcategoryResponse.getName(), result.getContent().get(0).getName());
    }

    @Test
    void shouldDeleteSubcategory_whenSubcategoryExists() {
        // Given
        when(subcategoryRepository.findById(1L)).thenReturn(Optional.of(testSubcategory));

        // When
        subcategoryService.deleteSubcategory(1L);

        // Then
        verify(subcategoryRepository).delete(testSubcategory);
    }

    @Test
    void shouldThrowException_whenDeleteSubcategoryNotFound() {
        // Given
        when(subcategoryRepository.findById(1L)).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> subcategoryService.deleteSubcategory(1L));
        assertEquals("Subcategory not found with id: 1", exception.getMessage());
        verify(subcategoryRepository, never()).delete(any());
    }

    @Test
    void shouldToggleStatus_whenUpdateSubcategoryStatus() {
        // Given
        when(subcategoryRepository.findById(1L)).thenReturn(Optional.of(testSubcategory));
        when(subcategoryRepository.save(testSubcategory)).thenReturn(testSubcategory);
        when(subcategoryMapper.toResponse(testSubcategory)).thenReturn(subcategoryResponse);

        // When
        SubcategoryResponse result = subcategoryService.updateSubcategoryStatus(1L, false);

        // Then
        assertNotNull(result);
        assertFalse(testSubcategory.getIsActive());
        verify(subcategoryRepository).save(testSubcategory);
    }

    @Test
    void shouldReturnActiveSubcategories_whenGetActiveSubcategories() {
        // Given
        List<Subcategory> subcategories = Arrays.asList(testSubcategory);
        Page<Subcategory> subcategoryPage = new PageImpl<>(subcategories);
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"));
        
        when(subcategoryRepository.findByIsActiveTrue(pageable)).thenReturn(subcategoryPage);
        when(subcategoryMapper.toResponse(testSubcategory)).thenReturn(subcategoryResponse);

        // When
        Page<SubcategoryResponse> result = subcategoryService.getActiveSubcategories(0, 10, "name", "asc");

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(subcategoryResponse.getName(), result.getContent().get(0).getName());
    }

    @Test
    void shouldReturnActiveSubcategory_whenGetActiveSubcategoryBySlug() {
        // Given
        when(subcategoryRepository.findBySlugAndIsActiveTrue("smartphones")).thenReturn(Optional.of(testSubcategory));
        when(subcategoryMapper.toResponse(testSubcategory)).thenReturn(subcategoryResponse);

        // When
        SubcategoryResponse result = subcategoryService.getActiveSubcategoryBySlug("smartphones");

        // Then
        assertNotNull(result);
        assertEquals(subcategoryResponse.getSlug(), result.getSlug());
    }

    @Test
    void shouldThrowException_whenGetActiveSubcategoryBySlugNotFound() {
        // Given
        when(subcategoryRepository.findBySlugAndIsActiveTrue("nonexistent")).thenReturn(Optional.empty());

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> subcategoryService.getActiveSubcategoryBySlug("nonexistent"));
        assertEquals("Active subcategory not found with slug: nonexistent", exception.getMessage());
    }

    @Test
    void shouldReturnActiveSubcategoriesByCategorySlug_whenValidSlug() {
        // Given
        List<Subcategory> subcategories = Arrays.asList(testSubcategory);
        Page<Subcategory> subcategoryPage = new PageImpl<>(subcategories);
        Pageable pageable = PageRequest.of(0, 10, Sort.by(Sort.Direction.ASC, "name"));
        
        when(subcategoryRepository.findByCategorySlugAndIsActiveTrue("electronics", pageable)).thenReturn(subcategoryPage);
        when(subcategoryMapper.toResponse(testSubcategory)).thenReturn(subcategoryResponse);

        // When
        Page<SubcategoryResponse> result = subcategoryService.getActiveSubcategoriesByCategorySlug("electronics", 0, 10, "name", "asc");

        // Then
        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(subcategoryResponse.getName(), result.getContent().get(0).getName());
    }
}
