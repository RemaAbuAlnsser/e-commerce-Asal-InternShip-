package com.asal.ecommerce.controller.admin;

import com.asal.ecommerce.dto.CategoryCreateRequest;
import com.asal.ecommerce.dto.CategoryResponse;
import com.asal.ecommerce.dto.CategoryUpdateRequest;
import com.asal.ecommerce.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminCategoryControllerTest {

    @Mock
    private CategoryService categoryService;

    @InjectMocks
    private CategoryController categoryController;

    private CategoryResponse categoryResponse;
    private CategoryCreateRequest createRequest;
    private CategoryUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        categoryResponse = new CategoryResponse();
        categoryResponse.setId(1L);
        categoryResponse.setName("Electronics");
        categoryResponse.setSlug("electronics");
        categoryResponse.setDescription("Electronic devices");
        categoryResponse.setIsActive(true);

        createRequest = new CategoryCreateRequest();
        createRequest.setName("Electronics");
        createRequest.setDescription("Electronic devices");

        updateRequest = new CategoryUpdateRequest();
        updateRequest.setName("Updated Electronics");
        updateRequest.setDescription("Updated description");
    }

    @Test
    void shouldReturn201_whenCreateCategoryWithValidData() {
        // Given
        when(categoryService.createCategory(any(CategoryCreateRequest.class))).thenReturn(categoryResponse);

        // When
        ResponseEntity<CategoryResponse> response = categoryController.createCategory(createRequest);

        // Then
        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("Electronics", response.getBody().getName());
        assertEquals("electronics", response.getBody().getSlug());
        assertTrue(response.getBody().getIsActive());
        verify(categoryService).createCategory(any(CategoryCreateRequest.class));
    }

    @Test
    void shouldThrowException_whenCreateCategoryWithDuplicateName() {
        // Given
        when(categoryService.createCategory(any(CategoryCreateRequest.class)))
                .thenThrow(new RuntimeException("Category with name 'Electronics' already exists"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> categoryController.createCategory(createRequest));
        assertEquals("Category with name 'Electronics' already exists", exception.getMessage());
        verify(categoryService).createCategory(any(CategoryCreateRequest.class));
    }

    @Test
    void shouldReturn200_whenUpdateCategoryWithValidData() {
        // Given
        when(categoryService.updateCategory(eq(1L), any(CategoryUpdateRequest.class))).thenReturn(categoryResponse);

        // When
        ResponseEntity<CategoryResponse> response = categoryController.updateCategory(1L, updateRequest);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        verify(categoryService).updateCategory(eq(1L), any(CategoryUpdateRequest.class));
    }

    @Test
    void shouldThrowException_whenUpdateCategoryNotFound() {
        // Given
        when(categoryService.updateCategory(eq(999L), any(CategoryUpdateRequest.class)))
                .thenThrow(new RuntimeException("Category not found with id: 999"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> categoryController.updateCategory(999L, updateRequest));
        assertEquals("Category not found with id: 999", exception.getMessage());
        verify(categoryService).updateCategory(eq(999L), any(CategoryUpdateRequest.class));
    }

    @Test
    void shouldReturn200_whenGetAllCategories() {
        // Given
        Page<CategoryResponse> categoryPage = new PageImpl<>(Arrays.asList(categoryResponse));
        when(categoryService.getAllCategories(0, 10, "name", "asc")).thenReturn(categoryPage);

        // When
        ResponseEntity<Page<CategoryResponse>> response = categoryController.getAllCategories(0, 10, "name", "asc");

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1, response.getBody().getTotalElements());
        assertEquals("Electronics", response.getBody().getContent().get(0).getName());
        verify(categoryService).getAllCategories(0, 10, "name", "asc");
    }

    @Test
    void shouldReturn200_whenGetCategoryById() {
        // Given
        when(categoryService.getCategoryById(1L)).thenReturn(categoryResponse);

        // When
        ResponseEntity<CategoryResponse> response = categoryController.getCategoryById(1L);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertEquals("Electronics", response.getBody().getName());
        assertEquals("electronics", response.getBody().getSlug());
        verify(categoryService).getCategoryById(1L);
    }

    @Test
    void shouldThrowException_whenGetCategoryByIdNotFound() {
        // Given
        when(categoryService.getCategoryById(999L))
                .thenThrow(new RuntimeException("Category not found with id: 999"));

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> categoryController.getCategoryById(999L));
        assertEquals("Category not found with id: 999", exception.getMessage());
        verify(categoryService).getCategoryById(999L);
    }

    @Test
    void shouldReturn200_whenSearchCategories() {
        // Given
        Page<CategoryResponse> categoryPage = new PageImpl<>(Arrays.asList(categoryResponse));
        when(categoryService.searchCategories("Electronics", 0, 10, "name", "asc")).thenReturn(categoryPage);

        // When
        ResponseEntity<Page<CategoryResponse>> response = categoryController.searchCategories("Electronics", 0, 10, "name", "asc");

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals("Electronics", response.getBody().getContent().get(0).getName());
        assertEquals(1, response.getBody().getTotalElements());
        verify(categoryService).searchCategories("Electronics", 0, 10, "name", "asc");
    }

    @Test
    void shouldReturn204_whenDeleteCategory() {
        // Given
        doNothing().when(categoryService).deleteCategory(1L);

        // When
        ResponseEntity<Void> response = categoryController.deleteCategory(1L);

        // Then
        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        verify(categoryService).deleteCategory(1L);
    }

    @Test
    void shouldThrowException_whenDeleteCategoryNotFound() {
        // Given
        doThrow(new RuntimeException("Category not found with id: 999"))
                .when(categoryService).deleteCategory(999L);

        // When & Then
        RuntimeException exception = assertThrows(RuntimeException.class, 
            () -> categoryController.deleteCategory(999L));
        assertEquals("Category not found with id: 999", exception.getMessage());
        verify(categoryService).deleteCategory(999L);
    }

    @Test
    void shouldReturn200_whenToggleStatus() {
        // Given
        when(categoryService.updateCategoryStatus(1L, true)).thenReturn(categoryResponse);

        // When
        ResponseEntity<CategoryResponse> response = categoryController.updateCategoryStatus(1L, true);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertTrue(response.getBody().getIsActive());
        verify(categoryService).updateCategoryStatus(1L, true);
    }

    @Test
    void shouldReturn200_whenToggleStatusToFalse() {
        // Given
        categoryResponse.setIsActive(false);
        when(categoryService.updateCategoryStatus(1L, false)).thenReturn(categoryResponse);

        // When
        ResponseEntity<CategoryResponse> response = categoryController.updateCategoryStatus(1L, false);

        // Then
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertNotNull(response.getBody());
        assertEquals(1L, response.getBody().getId());
        assertFalse(response.getBody().getIsActive());
        verify(categoryService).updateCategoryStatus(1L, false);
    }
}
