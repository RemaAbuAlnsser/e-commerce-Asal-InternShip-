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
    
    public CategoryResponse updateCategory(Long id, CategoryUpdateRequest request) {
        Category category = categoryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Category not found with id: " + id));
        
        // Create temporary category to get validated name and slug
        String name = request.getName().trim();
        String slug = categoryMapper.generateSlug(request.getSlug(), name);
        
        // Validate duplicate name (excluding current category)
        if (categoryRepository.existsByNameIgnoreCaseAndIdNot(name, id)) {
            throw new RuntimeException("Category with name '" + name + "' already exists");
        }
        
        // Validate duplicate slug (excluding current category)
        if (categoryRepository.existsBySlugIgnoreCaseAndIdNot(slug, id)) {
            throw new RuntimeException("Category with slug '" + slug + "' already exists");
        }
        
        // Update entity using mapper
        categoryMapper.updateEntity(category, request);
        
        Category savedCategory = categoryRepository.save(category);
        return categoryMapper.toResponse(savedCategory);
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
    
}
