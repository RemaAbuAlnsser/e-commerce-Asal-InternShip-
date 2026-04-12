package com.asal.ecommerce.controller.customer;

import com.asal.ecommerce.dto.SiteImageResponse;
import com.asal.ecommerce.service.SiteImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController("customerSiteImageController")
@RequestMapping("/api/site-images")
public class SiteImageController {

    @Autowired
    private SiteImageService siteImageService;

    @GetMapping
    public ResponseEntity<List<SiteImageResponse>> getAllSiteImages() {
        List<SiteImageResponse> response = siteImageService.getAllSiteImages();
        return ResponseEntity.ok(response);
    }
}
