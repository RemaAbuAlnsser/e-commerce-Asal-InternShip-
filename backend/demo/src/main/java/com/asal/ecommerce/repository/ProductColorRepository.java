// ProductColorRepository.java
package com.asal.ecommerce.repository;

import com.asal.ecommerce.model.ProductColor;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductColorRepository extends JpaRepository<ProductColor, Long> {
    List<ProductColor> findByProductId(Long productId);
    void deleteByProductId(Long productId);
}