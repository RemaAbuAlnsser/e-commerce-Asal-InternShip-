// ProductColorRepository.java
package com.asal.ecommerce.repository;

import com.asal.ecommerce.model.ProductColor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface ProductColorRepository extends JpaRepository<ProductColor, Long> {
    List<ProductColor> findByProductId(Long productId);
    void deleteByProductId(Long productId);

    @Query("SELECT COALESCE(SUM(pc.stock), 0) FROM ProductColor pc WHERE pc.product.id = :productId")
    int sumStockByProductId(@Param("productId") Long productId);
}