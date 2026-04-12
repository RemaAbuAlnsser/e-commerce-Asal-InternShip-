package com.asal.ecommerce.repository;

import com.asal.ecommerce.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ProductRepository extends JpaRepository<Product, Long> {

    boolean existsBySku(String sku);

    Optional<Product> findBySku(String sku);

    // ── filtered listing ──────────────────────────────────────────────────────
    // null param = ignore that filter
    @Query("""
            SELECT p FROM Product p
            WHERE (:status        IS NULL OR p.status         = :status)
            AND   (:categoryId    IS NULL OR p.category.id    = :categoryId)
            AND   (:subcategoryId IS NULL OR p.subcategory.id = :subcategoryId)
            AND   (:brandId       IS NULL OR p.brand.id       = :brandId)
            AND   (:isFeatured    IS NULL OR p.featured       = :isFeatured)
            AND   (:isExclusive   IS NULL OR p.exclusive      = :isExclusive)
            """)
    Page<Product> findAllWithFilters(
            @Param("status")        String  status,
            @Param("categoryId")    Long    categoryId,
            @Param("subcategoryId") Long    subcategoryId,
            @Param("brandId")       Long    brandId,
            @Param("isFeatured")    Boolean isFeatured,
            @Param("isExclusive")   Boolean isExclusive,
            Pageable pageable
    );

    // ── keyword search on name + description ──────────────────────────────────
    @Query("""
            SELECT p FROM Product p
            WHERE p.status = 'active'
            AND (LOWER(p.name)        LIKE LOWER(CONCAT('%', :keyword, '%'))
              OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%')))
            """)
    Page<Product> searchByKeyword(@Param("keyword") String keyword, Pageable pageable);

    // ── stock alerts ──────────────────────────────────────────────────────────
    @Query("SELECT p FROM Product p WHERE p.stock = 0")
    List<Product> findOutOfStockProducts();

    @Query("SELECT p FROM Product p WHERE p.stock > 0 AND p.stock <= :threshold ORDER BY p.stock ASC")
    List<Product> findLowStockProducts(@Param("threshold") int threshold);
}