package com.asal.ecommerce.service;

import com.asal.ecommerce.dto.*;
import com.asal.ecommerce.model.Brand;
import com.asal.ecommerce.repository.BrandRepository;
import com.asal.ecommerce.mapper.BrandMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class BrandService {
    
    @Autowired
    private BrandRepository brandRepository;
    
    @Autowired
    private BrandMapper brandMapper;

    public BrandResponse createBrand(BrandCreateRequest request) {
        Brand brand = brandMapper.toEntity(request);

        if (brandRepository.existsByNameIgnoreCase(brand.getName())) {
            throw new RuntimeException("Brand with name '" + brand.getName() + "' already exists");
        }

        Brand savedBrand = brandRepository.save(brand);
        return brandMapper.toResponse(savedBrand);
    }

    public BrandResponse updateBrand(Long id, BrandUpdateRequest request) {
        Brand brand = brandRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Brand not found with id: " + id));

        if (!brand.getName().equals(request.getName())) {
            if (brandRepository.existsByNameIgnoreCaseAndIdNot(request.getName(), id)) {
                throw new RuntimeException("Brand with name '" + request.getName() + "' already exists");
            }
        }

        brandMapper.updateEntity(brand, request);
        Brand savedBrand = brandRepository.save(brand);
        return brandMapper.toResponse(savedBrand);
    }
    
    @Transactional(readOnly = true)
    public BrandResponse getBrandById(Long id) {
        Brand brand = brandRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Brand not found with id: " + id));
        return brandMapper.toResponse(brand);
    }
    
    @Transactional(readOnly = true)
    public Page<BrandResponse> getAllBrands(int page, int size, String sortBy, String direction) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Brand> brands = brandRepository.findAll(pageable);
        return brands.map(brandMapper::toResponse);
    }
    
    @Transactional(readOnly = true)
    public Page<BrandResponse> searchBrands(String name, int page, int size, String sortBy, String direction) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Brand> brands = brandRepository.findByNameContainingIgnoreCase(name, pageable);
        return brands.map(brandMapper::toResponse);
    }
    
    public void deleteBrand(Long id) {
        Brand brand = brandRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Brand not found with id: " + id));
        
        brandRepository.delete(brand);
    }
    
    public BrandResponse updateBrandStatus(Long id, Boolean isActive) {
        Brand brand = brandRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Brand not found with id: " + id));
        
        brand.setIsActive(isActive);
        Brand savedBrand = brandRepository.save(brand);
        
        return brandMapper.toResponse(savedBrand);
    }
    
    @Transactional(readOnly = true)
    public boolean brandExists(Long id) {
        return brandRepository.existsById(id);
    }
    
    @Transactional(readOnly = true)
    public Page<BrandResponse> getActiveBrands(int page, int size, String sortBy, String direction) {
        Sort sort = Sort.by(Sort.Direction.fromString(direction), sortBy);
        Pageable pageable = PageRequest.of(page, size, sort);
        
        Page<Brand> brands = brandRepository.findByIsActiveTrue(pageable);
        return brands.map(brandMapper::toResponse);
    }
    
    @Transactional(readOnly = true)
    public BrandResponse getActiveBrandById(Long id) {
        Brand brand = brandRepository.findByIdAndIsActiveTrue(id)
            .orElseThrow(() -> new RuntimeException("Active brand not found with id: " + id));
        return brandMapper.toResponse(brand);
    }
    
    @Transactional(readOnly = true)
    public Long getBrandCount() {
        return brandRepository.count();
    }
}
