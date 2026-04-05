package com.asal.ecommerce.service;

import com.asal.ecommerce.dto.*;
import com.asal.ecommerce.model.Category;
import com.asal.ecommerce.repository.CategoryRepository;
import com.asal.ecommerce.mapper.CategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


@Service
@Transactional
public class CategoryService {
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private CategoryMapper categoryMapper;

    /**
     * Creates a new category.
     *
     * @param request the category creation request
     * @return the created category response
     */
    public CategoryResponse createCategory(CategoryCreateRequest request) {
        // Create entity using mapper
        Category category = categoryMapper.toEntity(request);

        // Validate duplicate name
        if (categoryRepository.existsByNameIgnoreCase(category.getName())) {
            throw new RuntimeException("Category with name '" + category.getName() + "' already exists");
        }

        // Validate duplicate slug
        if (categoryRepository.existsBySlugIgnoreCase(category.getSlug())) {
            throw new RuntimeException("Category with slug '" + category.getSlug() + "' already exists");
        }

        Category savedCategory = categoryRepository.save(category);
        return categoryMapper.toResponse(savedCategory);
    }

    /**
     * Updates an existing category.
     *
     * @param id      the category ID
     * @param request the category update request
     * @return the updated category response
     */
    public CategoryResponse updateCategory(Long id, CategoryUpdateRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));

        // Check if name is being changed and if new name already exists
        if (!category.getName().equals(request.getName())) {
            if (categoryRepository.existsByNameIgnoreCaseAndIdNot(request.getName(), id)) {
                throw new RuntimeException("Category with name '" + request.getName() + "' already exists");
            }
        }

        // Check if slug is being changed and if new slug already exists
        if (request.getSlug() != null && !category.getSlug().equals(request.getSlug())) {
            if (categoryRepository.existsBySlugIgnoreCaseAndIdNot(request.getSlug(), id)) {
                throw new RuntimeException("Category with slug '" + request.getSlug() + "' already exists");
            }
        }

        // Store the old active status to check if it changed
        boolean wasActive = category.getIsActive();
        
        categoryMapper.updateEntity(category, request);
        Category savedCategory = categoryRepository.save(category);
        
        // If category was deactivated, deactivate all its subcategories
        if (wasActive && !savedCategory.getIsActive()) {
            deactivateSubcategoriesByCategory(id);
        }
        
        return categoryMapper.toResponse(savedCategory);
    }
    
    private void deactivateSubcategoriesByCategory(Long categoryId) {
        // Update all subcategories to inactive for the given category
        categoryRepository.deactivateSubcategoriesByCategory(categoryId);
    }
    
    @Transactional(readOnly = true)
    public CategoryResponse getCategoryById(Long id) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        return categoryMapper.toResponse(category);
    }
    
    @Transactional(readOnly = true)
    public Page<CategoryResponse> getAllCategories(int page, int size, String sortBy, String direction) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Category> categories = categoryRepository.findAll(pageable);
        return categories.map(categoryMapper::toResponse);
    }
    
    @Transactional(readOnly = true)
    public Page<CategoryResponse> searchCategories(String name, int page, int size, String sortBy, String direction) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Category> categories = categoryRepository.findByNameContainingIgnoreCase(name, pageable);
        return categories.map(categoryMapper::toResponse);
    }
    
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        
        // Check if category has subcategories
        if (category.getSubcategories() != null && !category.getSubcategories().isEmpty()) {
            throw new RuntimeException("Cannot delete category with existing subcategories");
        }
        
        categoryRepository.delete(category);
    }
    
    public CategoryResponse updateCategoryStatus(Long id, Boolean isActive) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        
        category.setIsActive(isActive);
        Category savedCategory = categoryRepository.save(category);
        
        // If category is being deactivated, deactivate all its subcategories
        if (!isActive) {
            deactivateSubcategoriesByCategory(id);
        }
        
        return categoryMapper.toResponse(savedCategory);
    }
    
    @Transactional(readOnly = true)
    public boolean categoryExists(Long id) {
        return categoryRepository.existsById(id);
    }
    
    // Customer public methods - return only active data
    @Transactional(readOnly = true)
    public Page<CategoryResponse> getActiveCategories(int page, int size, String sortBy, String direction) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Category> categories = categoryRepository.findByIsActiveTrue(pageable);
        return categories.map(categoryMapper::toResponse);
    }
    
    @Transactional(readOnly = true)
    public CategoryResponse getActiveCategoryBySlug(String slug) {
        Category category = categoryRepository.findBySlugAndIsActiveTrue(slug)
            .orElseThrow(() -> new RuntimeException("Active category not found with slug: " + slug));
        return categoryMapper.toResponse(category);
    }
    
    @Transactional(readOnly = true)
    public Long getCategoryCount() {
        return categoryRepository.count();
    }
    
}
