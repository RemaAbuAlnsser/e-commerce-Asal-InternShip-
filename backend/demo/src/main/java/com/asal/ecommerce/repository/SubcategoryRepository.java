package com.asal.ecommerce.repository;

import com.asal.ecommerce.model.Subcategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface SubcategoryRepository extends JpaRepository<Subcategory, Long> {
    
    boolean existsByNameIgnoreCaseAndCategoryId(String name, Long categoryId);
    
    boolean existsBySlugIgnoreCaseAndCategoryId(String slug, Long categoryId);
    
    boolean existsByNameIgnoreCaseAndCategoryIdAndIdNot(String name, Long categoryId, Long id);
    
    boolean existsBySlugIgnoreCaseAndCategoryIdAndIdNot(String slug, Long categoryId, Long id);
    
    Optional<Subcategory> findBySlugAndCategoryId(String slug, Long categoryId);
    
    Page<Subcategory> findByCategoryId(Long categoryId, Pageable pageable);
    
    Page<Subcategory> findByCategoryIdAndIsActive(Long categoryId, Boolean isActive, Pageable pageable);
    
    @Query("SELECT s FROM Subcategory s WHERE LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Subcategory> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);
    
    @Query("SELECT s FROM Subcategory s WHERE s.category.id = :categoryId AND LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Subcategory> findByCategoryIdAndNameContainingIgnoreCase(@Param("categoryId") Long categoryId, @Param("name") String name, Pageable pageable);
    
    Page<Subcategory> findByIsActive(Boolean isActive, Pageable pageable);
    
    @Query("SELECT s FROM Subcategory s WHERE s.isActive = :isActive AND LOWER(s.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Subcategory> findByIsActiveAndNameContainingIgnoreCase(@Param("isActive") Boolean isActive, @Param("name") String name, Pageable pageable);
}
