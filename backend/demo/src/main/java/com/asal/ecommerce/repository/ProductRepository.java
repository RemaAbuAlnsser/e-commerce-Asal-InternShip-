package com.asal.ecommerce.repository;

import com.asal.ecommerce.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsBySku(String sku);

    boolean existsBySkuAndIdNot(String sku, Long id);

    Optional<Product> findBySku(String sku);

    // ── Filtered listing (admin & customer share the same query, status filter is optional) ──

    @Query("""
        SELECT p FROM Product p
        LEFT JOIN FETCH p.category
        LEFT JOIN FETCH p.subcategory
        LEFT JOIN FETCH p.brand
        WHERE (:categoryId    IS NULL OR p.category.id    = :categoryId)
          AND (:subcategoryId IS NULL OR p.subcategory.id = :subcategoryId)
          AND (:brandId       IS NULL OR p.brand.id       = :brandId)
          AND (:status        IS NULL OR p.status         = :status)
          AND (:featured      IS NULL OR p.isFeatured     = :featured)
          AND (:exclusive     IS NULL OR p.isExclusive    = :exclusive)
        """)
    Page<Product> findAllFiltered(
            @Param("categoryId")    Long    categoryId,
            @Param("subcategoryId") Long    subcategoryId,
            @Param("brandId")       Long    brandId,
            @Param("status")        String  status,
            @Param("featured")      Boolean featured,
            @Param("exclusive")     Boolean exclusive,
            Pageable pageable
    );

    // ── Search by name or SKU ────────────────────────────────────────────────

    @Query("""
        SELECT p FROM Product p
        LEFT JOIN FETCH p.category
        LEFT JOIN FETCH p.subcategory
        LEFT JOIN FETCH p.brand
        WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(p.sku)  LIKE LOWER(CONCAT('%', :keyword, '%'))
        """)
    Page<Product> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);
}