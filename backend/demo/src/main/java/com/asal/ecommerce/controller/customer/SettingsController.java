package com.asal.ecommerce.controller.customer;

import com.asal.ecommerce.dto.SettingsResponse;
import com.asal.ecommerce.service.SettingsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController("customerSettingsController")
@RequestMapping("/api/settings")
public class SettingsController {

    @Autowired
    private SettingsService settingsService;

    @GetMapping
    public ResponseEntity<SettingsResponse> getSettings() {
        SettingsResponse response = settingsService.getSettings();
        return ResponseEntity.ok(response);
    }
}
