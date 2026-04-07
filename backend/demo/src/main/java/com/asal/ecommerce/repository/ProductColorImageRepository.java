// ProductColorImageRepository.java
package com.asal.ecommerce.repository;

import com.asal.ecommerce.model.ProductColorImage;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ProductColorImageRepository extends JpaRepository<ProductColorImage, Long> {
    List<ProductColorImage> findByProductColorId(Long colorId);
}