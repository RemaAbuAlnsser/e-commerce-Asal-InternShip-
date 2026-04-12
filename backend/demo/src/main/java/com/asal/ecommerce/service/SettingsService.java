package com.asal.ecommerce.service;

import com.asal.ecommerce.dto.SettingsResponse;
import com.asal.ecommerce.dto.SettingsUpdateRequest;
import com.asal.ecommerce.mapper.SettingsMapper;
import com.asal.ecommerce.model.Settings;
import com.asal.ecommerce.repository.SettingsRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class SettingsService {

    @Autowired
    private SettingsRepository settingsRepository;

    @Autowired
    private SettingsMapper settingsMapper;

    @Autowired
    private ImageUploadService imageUploadService;

    // Returns the singleton row, creating it if it does not exist yet
    @Transactional(readOnly = true)
    public SettingsResponse getSettings() {
        Settings settings = getOrCreate();
        return settingsMapper.toResponse(settings);
    }

    public SettingsResponse updateSettings(
            SettingsUpdateRequest request,
            MultipartFile logoFile,
            MultipartFile faviconFile,
            MultipartFile siteImageFile) throws Exception {

        Settings settings = getOrCreate();

        // Apply text fields
        settingsMapper.updateEntity(settings, request);

        // Handle logo upload
        if (logoFile != null && !logoFile.isEmpty()) {
            imageUploadService.deleteImage(settings.getSiteLogo());
            settings.setSiteLogo(imageUploadService.uploadSettingsImage(logoFile));
        }

        // Handle favicon upload
        if (faviconFile != null && !faviconFile.isEmpty()) {
            imageUploadService.deleteImage(settings.getSiteFavicon());
            settings.setSiteFavicon(imageUploadService.uploadSettingsImage(faviconFile));
        }

        // Handle main site image upload
        if (siteImageFile != null && !siteImageFile.isEmpty()) {
            imageUploadService.deleteImage(settings.getSiteImage());
            settings.setSiteImage(imageUploadService.uploadSettingsImage(siteImageFile));
        }

        Settings saved = settingsRepository.save(settings);
        return settingsMapper.toResponse(saved);
    }

    private Settings getOrCreate() {
        return settingsRepository.findAll()
                .stream()
                .findFirst()
                .orElseGet(() -> settingsRepository.save(new Settings()));
    }
}
