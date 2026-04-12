package com.asal.ecommerce.controller.admin;

import com.asal.ecommerce.dto.SiteImageOrderRequest;
import com.asal.ecommerce.dto.SiteImageResponse;
import com.asal.ecommerce.service.SiteImageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController("adminSiteImageController")
@RequestMapping("/api/admin/site-images")
public class SiteImageController {

    @Autowired
    private SiteImageService siteImageService;

    @GetMapping
    public ResponseEntity<List<SiteImageResponse>> getAllSiteImages() {
        try {
            List<SiteImageResponse> response = siteImageService.getAllSiteImages();
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            throw e;
        }
    }

    @PostMapping
    public ResponseEntity<SiteImageResponse> addSiteImage(
            @RequestParam("image") MultipartFile imageFile,
            @RequestParam(value = "displayOrder", defaultValue = "0") Integer displayOrder) {
        try {
            SiteImageResponse response = siteImageService.addSiteImage(imageFile, displayOrder);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PatchMapping("/{id}/order")
    public ResponseEntity<SiteImageResponse> updateDisplayOrder(
            @PathVariable Long id,
            @RequestBody SiteImageOrderRequest request) {
        try {
            SiteImageResponse response = siteImageService.updateDisplayOrder(id, request.getDisplayOrder());
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            throw e;
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSiteImage(@PathVariable Long id) {
        try {
            siteImageService.deleteSiteImage(id);
            return ResponseEntity.noContent().build();
        } catch (RuntimeException e) {
            throw e;
        }
    }
}
