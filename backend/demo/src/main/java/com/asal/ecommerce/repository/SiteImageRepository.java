package com.asal.ecommerce.repository;

import com.asal.ecommerce.model.SiteImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SiteImageRepository extends JpaRepository<SiteImage, Long> {
    List<SiteImage> findAllByOrderByDisplayOrderAsc();
}
