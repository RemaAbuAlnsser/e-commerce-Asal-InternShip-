package com.asal.ecommerce.service;

import com.asal.ecommerce.dto.*;
import com.asal.ecommerce.model.Category;
import com.asal.ecommerce.model.Subcategory;
import com.asal.ecommerce.repository.CategoryRepository;
import com.asal.ecommerce.repository.SubcategoryRepository;
import com.asal.ecommerce.mapper.SubcategoryMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class SubcategoryService {
    
    @Autowired
    private SubcategoryRepository subcategoryRepository;
    
    @Autowired
    private CategoryRepository categoryRepository;
    
    @Autowired
    private SubcategoryMapper subcategoryMapper;
    
    public SubcategoryResponse createSubcategory(SubcategoryCreateRequest request) {
        // Validate category exists
        Category category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new RuntimeException("Category not found with id: " + request.getCategoryId()));
        
        // Create entity using mapper
        Subcategory subcategory = subcategoryMapper.toEntity(request, category);
        
        // Validate duplicate name within the same category
        if (subcategoryRepository.existsByNameIgnoreCaseAndCategoryId(subcategory.getName(), request.getCategoryId())) {
            throw new RuntimeException("Subcategory with name '" + subcategory.getName() + "' already exists in this category");
        }
        
        // Validate duplicate slug within the same category
        if (subcategoryRepository.existsBySlugIgnoreCaseAndCategoryId(subcategory.getSlug(), request.getCategoryId())) {
            throw new RuntimeException("Subcategory with slug '" + subcategory.getSlug() + "' already exists in this category");
        }
        
        Subcategory savedSubcategory = subcategoryRepository.save(subcategory);
        return subcategoryMapper.toResponse(savedSubcategory);
    }
    
    public SubcategoryResponse updateSubcategory(Long id, SubcategoryUpdateRequest request) {
        Subcategory subcategory = subcategoryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Subcategory not found with id: " + id));
        
        // Validate category exists
        Category category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new RuntimeException("Category not found with id: " + request.getCategoryId()));
        
        // Get name and slug for validation
        String name = request.getName().trim();
        String slug = subcategoryMapper.generateSlug(request.getSlug(), name);
        
        // Validate duplicate name within the same category (excluding current subcategory)
        if (subcategoryRepository.existsByNameIgnoreCaseAndCategoryIdAndIdNot(name, request.getCategoryId(), id)) {
            throw new RuntimeException("Subcategory with name '" + name + "' already exists in this category");
        }
        
        // Validate duplicate slug within the same category (excluding current subcategory)
        if (subcategoryRepository.existsBySlugIgnoreCaseAndCategoryIdAndIdNot(slug, request.getCategoryId(), id)) {
            throw new RuntimeException("Subcategory with slug '" + slug + "' already exists in this category");
        }
        
        // Update entity using mapper
        subcategoryMapper.updateEntity(subcategory, request, category);
        
        Subcategory savedSubcategory = subcategoryRepository.save(subcategory);
        return subcategoryMapper.toResponse(savedSubcategory);
    }
    
    @Transactional(readOnly = true)
    public SubcategoryResponse getSubcategoryById(Long id) {
        Subcategory subcategory = subcategoryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Subcategory not found with id: " + id));
        return subcategoryMapper.toResponse(subcategory);
    }
    
    @Transactional(readOnly = true)
    public Page<SubcategoryResponse> getAllSubcategories(int page, int size, String sortBy, String direction) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Subcategory> subcategories = subcategoryRepository.findAll(pageable);
        return subcategories.map(subcategoryMapper::toResponse);
    }
    
    @Transactional(readOnly = true)
    public Page<SubcategoryResponse> getSubcategoriesByCategory(Long categoryId, int page, int size, String sortBy, String direction) {
        // Validate category exists
        if (!categoryRepository.existsById(categoryId)) {
            throw new RuntimeException("Category not found with id: " + categoryId);
        }
        
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Subcategory> subcategories = subcategoryRepository.findByCategoryId(categoryId, pageable);
        return subcategories.map(subcategoryMapper::toResponse);
    }
    
    @Transactional(readOnly = true)
    public Page<SubcategoryResponse> searchSubcategories(String name, int page, int size, String sortBy, String direction) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Subcategory> subcategories = subcategoryRepository.findByNameContainingIgnoreCase(name, pageable);
        return subcategories.map(subcategoryMapper::toResponse);
    }
    
    @Transactional(readOnly = true)
    public Page<SubcategoryResponse> searchSubcategoriesByCategory(Long categoryId, String name, int page, int size, String sortBy, String direction) {
        // Validate category exists
        if (!categoryRepository.existsById(categoryId)) {
            throw new RuntimeException("Category not found with id: " + categoryId);
        }
        
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Subcategory> subcategories = subcategoryRepository.findByCategoryIdAndNameContainingIgnoreCase(categoryId, name, pageable);
        return subcategories.map(subcategoryMapper::toResponse);
    }
    
    public void deleteSubcategory(Long id) {
        Subcategory subcategory = subcategoryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Subcategory not found with id: " + id));
        
        subcategoryRepository.delete(subcategory);
    }
    
    public SubcategoryResponse updateSubcategoryStatus(Long id, Boolean isActive) {
        Subcategory subcategory = subcategoryRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Subcategory not found with id: " + id));
        
        subcategory.setIsActive(isActive);
        Subcategory savedSubcategory = subcategoryRepository.save(subcategory);
        return subcategoryMapper.toResponse(savedSubcategory);
    }
    
    // Customer public methods - return only active data
    @Transactional(readOnly = true)
    public Page<SubcategoryResponse> getActiveSubcategories(int page, int size, String sortBy, String direction) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Subcategory> subcategories = subcategoryRepository.findByIsActiveTrue(pageable);
        return subcategories.map(subcategoryMapper::toResponse);
    }
    
    @Transactional(readOnly = true)
    public SubcategoryResponse getActiveSubcategoryBySlug(String slug) {
        Subcategory subcategory = subcategoryRepository.findBySlugAndIsActiveTrue(slug)
            .orElseThrow(() -> new RuntimeException("Active subcategory not found with slug: " + slug));
        return subcategoryMapper.toResponse(subcategory);
    }
    
    @Transactional(readOnly = true)
    public Page<SubcategoryResponse> getActiveSubcategoriesByCategorySlug(String categorySlug, int page, int size, String sortBy, String direction) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Subcategory> subcategories = subcategoryRepository.findByCategorySlugAndIsActiveTrue(categorySlug, pageable);
        return subcategories.map(subcategoryMapper::toResponse);
    }
    
    @Transactional(readOnly = true)
    public Long getSubcategoryCount() {
        return subcategoryRepository.count();
    }
    
    @Transactional(readOnly = true)
    public Long getSubcategoryCountByCategory(Long categoryId) {
        return subcategoryRepository.countByCategoryId(categoryId);
    }
    
}
