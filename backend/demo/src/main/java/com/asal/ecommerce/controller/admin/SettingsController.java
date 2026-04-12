package com.asal.ecommerce.controller.admin;

import com.asal.ecommerce.dto.SettingsResponse;
import com.asal.ecommerce.dto.SettingsUpdateRequest;
import com.asal.ecommerce.service.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController("adminSettingsController")
@RequestMapping("/api/admin/settings")
public class SettingsController {

    @Autowired
    private SettingsService settingsService;

    @GetMapping
    public ResponseEntity<SettingsResponse> getSettings() {
        try {
            SettingsResponse response = settingsService.getSettings();
            return ResponseEntity.ok(response);
        } catch (RuntimeException e) {
            throw e;
        }
    }

    @PutMapping
    public ResponseEntity<SettingsResponse> updateSettings(
            @RequestPart(value = "data", required = false) SettingsUpdateRequest request,
            @RequestPart(value = "logo", required = false) MultipartFile logoFile,
            @RequestPart(value = "favicon", required = false) MultipartFile faviconFile,
            @RequestPart(value = "siteImage", required = false) MultipartFile siteImageFile) {
        try {
            if (request == null) {
                request = new SettingsUpdateRequest();
            }
            SettingsResponse response = settingsService.updateSettings(request, logoFile, faviconFile, siteImageFile);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}
