package com.asal.ecommerce.repository;

import com.asal.ecommerce.model.Brand;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BrandRepository extends JpaRepository<Brand, Long> {
    
    boolean existsByNameIgnoreCase(String name);
    
    boolean existsByNameIgnoreCaseAndIdNot(String name, Long id);
    
    @Query("SELECT b FROM Brand b WHERE LOWER(b.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Brand> findByNameContainingIgnoreCase(@Param("name") String name, Pageable pageable);
    
    Page<Brand> findByIsActiveTrue(Pageable pageable);
    
    Optional<Brand> findByIdAndIsActiveTrue(Long id);
    
    Page<Brand> findByIsActive(Boolean isActive, Pageable pageable);
    
    @Query("SELECT b FROM Brand b WHERE b.isActive = :isActive AND LOWER(b.name) LIKE LOWER(CONCAT('%', :name, '%'))")
    Page<Brand> findByIsActiveAndNameContainingIgnoreCase(@Param("isActive") Boolean isActive, @Param("name") String name, Pageable pageable);
}
