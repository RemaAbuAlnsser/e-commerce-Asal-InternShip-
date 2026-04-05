package com.asal.ecommerce.repository;

import com.asal.ecommerce.model.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    boolean existsByNameIgnoreCase(String name);
    
    boolean existsBySlugIgnoreCase(String slug);
    
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
    
    boolean existsBySlugIgnoreCaseAndIdNot(String slug, Long id);
    
    Optional<Category> findBySlug(String slug);
    
    @Query("SELECT c FROM Category c WHERE LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Category> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);
    
    // Customer public methods - active data only
    Page<Category> findByIsActiveTrue(Pageable pageable);
    
    Optional<Category> findBySlugAndIsActiveTrue(String slug);
    
    Page<Category> findByIsActive(Boolean isActive, Pageable pageable);
    
    @Query("SELECT c FROM Category c WHERE c.isActive = :isActive AND LOWER(c.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Category> findByIsActiveAndNameContainingIgnoreCase(@Param("isActive") Boolean isActive, @Param("name") String name, Pageable pageable);
}
