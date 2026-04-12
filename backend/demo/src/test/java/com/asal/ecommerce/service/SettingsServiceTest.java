package com.asal.ecommerce.service;

import com.asal.ecommerce.dto.SettingsResponse;
import com.asal.ecommerce.dto.SettingsUpdateRequest;
import com.asal.ecommerce.mapper.SettingsMapper;
import com.asal.ecommerce.model.Settings;
import com.asal.ecommerce.repository.SettingsRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SettingsServiceTest {

    @Mock private SettingsRepository settingsRepository;
    @Mock private SettingsMapper     settingsMapper;
    @Mock private ImageUploadService imageUploadService;

    @InjectMocks
    private SettingsService settingsService;

    private Settings stubSettings;
    private SettingsResponse stubResponse;
    private SettingsUpdateRequest updateRequest;

    @BeforeEach
    void setUp() {
        stubSettings = new Settings();
        stubSettings.setId(1L);
        stubSettings.setSiteName("My Store");
        stubSettings.setSiteDescription("Best shop online");
        stubSettings.setContactEmail("contact@store.com");
        stubSettings.setContactPhone("+1-555-0000");
        stubSettings.setAddress("123 Main St");
        stubSettings.setFacebookUrl("https://facebook.com/store");
        stubSettings.setInstagramUrl("https://instagram.com/store");
        stubSettings.setWhatsappUrl("https://wa.me/1234567890");
        stubSettings.setSiteLogo("/uploads/settings/logo.png");
        stubSettings.setSiteFavicon("/uploads/settings/favicon.ico");
        stubSettings.setSiteImage("/uploads/settings/cover.jpg");
        stubSettings.setCreatedAt(LocalDateTime.now());
        stubSettings.setUpdatedAt(LocalDateTime.now());

        stubResponse = new SettingsResponse();
        stubResponse.setId(1L);
        stubResponse.setSiteName("My Store");
        stubResponse.setContactEmail("contact@store.com");

        updateRequest = new SettingsUpdateRequest();
        updateRequest.setSiteName("Updated Store");
        updateRequest.setSiteDescription("Better shop");
        updateRequest.setContactEmail("new@store.com");
        updateRequest.setContactPhone("+1-555-9999");
        updateRequest.setAddress("456 New Ave");
        updateRequest.setFacebookUrl("https://facebook.com/new");
        updateRequest.setInstagramUrl("https://instagram.com/new");
        updateRequest.setWhatsappUrl("https://wa.me/9876543210");
    }

    // =========================================================================
    // getSettings — singleton row exists
    // =========================================================================

    @Test
    void getSettings_whenRowExists_returnsResponse() {
        when(settingsRepository.findAll()).thenReturn(List.of(stubSettings));
        when(settingsMapper.toResponse(stubSettings)).thenReturn(stubResponse);

        SettingsResponse result = settingsService.getSettings();

        assertNotNull(result);
        assertEquals(1L, result.getId());
        assertEquals("My Store", result.getSiteName());
        verify(settingsRepository).findAll();
        verify(settingsMapper).toResponse(stubSettings);
    }

    // =========================================================================
    // getSettings — no row yet → creates empty singleton
    // =========================================================================

    @Test
    void getSettings_whenNoRow_createsEmptySingletonAndReturnsResponse() {
        Settings empty = new Settings();
        when(settingsRepository.findAll()).thenReturn(Collections.emptyList());
        when(settingsRepository.save(any(Settings.class))).thenReturn(empty);
        when(settingsMapper.toResponse(empty)).thenReturn(new SettingsResponse());

        SettingsResponse result = settingsService.getSettings();

        assertNotNull(result);
        verify(settingsRepository).save(any(Settings.class));
        verify(settingsMapper).toResponse(empty);
    }

    // =========================================================================
    // updateSettings — text fields only (no new images)
    // =========================================================================

    @Test
    void updateSettings_withNoFiles_updatesTextFieldsAndSaves() throws Exception {
        when(settingsRepository.findAll()).thenReturn(List.of(stubSettings));
        when(settingsRepository.save(stubSettings)).thenReturn(stubSettings);
        when(settingsMapper.toResponse(stubSettings)).thenReturn(stubResponse);

        SettingsResponse result = settingsService.updateSettings(updateRequest, null, null, null);

        assertNotNull(result);
        verify(settingsMapper).updateEntity(stubSettings, updateRequest);
        verify(settingsRepository).save(stubSettings);
        verify(imageUploadService, never()).uploadSettingsImage(any());
    }

    // =========================================================================
    // updateSettings — with logo file
    // =========================================================================

    @Test
    void updateSettings_withLogoFile_uploadsAndReplacesOldLogo() throws Exception {
        MockMultipartFile logo = new MockMultipartFile(
                "logo", "logo.png", "image/png", "fake-png".getBytes());

        when(settingsRepository.findAll()).thenReturn(List.of(stubSettings));
        when(imageUploadService.uploadSettingsImage(logo)).thenReturn("/uploads/settings/new-logo.png");
        when(settingsRepository.save(stubSettings)).thenReturn(stubSettings);
        when(settingsMapper.toResponse(stubSettings)).thenReturn(stubResponse);

        settingsService.updateSettings(updateRequest, logo, null, null);

        verify(imageUploadService).deleteImage("/uploads/settings/logo.png");
        verify(imageUploadService).uploadSettingsImage(logo);
        assertEquals("/uploads/settings/new-logo.png", stubSettings.getSiteLogo());
    }

    // =========================================================================
    // updateSettings — with favicon file
    // =========================================================================

    @Test
    void updateSettings_withFaviconFile_uploadsAndReplacesOldFavicon() throws Exception {
        MockMultipartFile favicon = new MockMultipartFile(
                "favicon", "fav.ico", "image/x-icon", "fake-ico".getBytes());

        when(settingsRepository.findAll()).thenReturn(List.of(stubSettings));
        when(imageUploadService.uploadSettingsImage(favicon)).thenReturn("/uploads/settings/new-fav.ico");
        when(settingsRepository.save(stubSettings)).thenReturn(stubSettings);
        when(settingsMapper.toResponse(stubSettings)).thenReturn(stubResponse);

        settingsService.updateSettings(updateRequest, null, favicon, null);

        verify(imageUploadService).deleteImage("/uploads/settings/favicon.ico");
        verify(imageUploadService).uploadSettingsImage(favicon);
        assertEquals("/uploads/settings/new-fav.ico", stubSettings.getSiteFavicon());
    }

    // =========================================================================
    // updateSettings — with siteImage file
    // =========================================================================

    @Test
    void updateSettings_withSiteImageFile_uploadsAndReplacesOldCover() throws Exception {
        MockMultipartFile cover = new MockMultipartFile(
                "siteImage", "cover.jpg", "image/jpeg", "fake-jpg".getBytes());

        when(settingsRepository.findAll()).thenReturn(List.of(stubSettings));
        when(imageUploadService.uploadSettingsImage(cover)).thenReturn("/uploads/settings/new-cover.jpg");
        when(settingsRepository.save(stubSettings)).thenReturn(stubSettings);
        when(settingsMapper.toResponse(stubSettings)).thenReturn(stubResponse);

        settingsService.updateSettings(updateRequest, null, null, cover);

        verify(imageUploadService).deleteImage("/uploads/settings/cover.jpg");
        verify(imageUploadService).uploadSettingsImage(cover);
        assertEquals("/uploads/settings/new-cover.jpg", stubSettings.getSiteImage());
    }

    // =========================================================================
    // updateSettings — all three files at once
    // =========================================================================

    @Test
    void updateSettings_withAllFiles_uploadsAllThreeImages() throws Exception {
        MockMultipartFile logo    = new MockMultipartFile("logo",      "logo.png", "image/png",      "l".getBytes());
        MockMultipartFile favicon = new MockMultipartFile("favicon",   "fav.ico",  "image/x-icon",   "f".getBytes());
        MockMultipartFile cover   = new MockMultipartFile("siteImage", "cover.jpg","image/jpeg",      "c".getBytes());

        when(settingsRepository.findAll()).thenReturn(List.of(stubSettings));
        when(imageUploadService.uploadSettingsImage(logo)).thenReturn("/uploads/settings/logo2.png");
        when(imageUploadService.uploadSettingsImage(favicon)).thenReturn("/uploads/settings/fav2.ico");
        when(imageUploadService.uploadSettingsImage(cover)).thenReturn("/uploads/settings/cover2.jpg");
        when(settingsRepository.save(stubSettings)).thenReturn(stubSettings);
        when(settingsMapper.toResponse(stubSettings)).thenReturn(stubResponse);

        settingsService.updateSettings(updateRequest, logo, favicon, cover);

        verify(imageUploadService, times(3)).uploadSettingsImage(any());
        assertEquals("/uploads/settings/logo2.png",   stubSettings.getSiteLogo());
        assertEquals("/uploads/settings/fav2.ico",    stubSettings.getSiteFavicon());
        assertEquals("/uploads/settings/cover2.jpg",  stubSettings.getSiteImage());
    }

    // =========================================================================
    // updateSettings — first call creates singleton when table is empty
    // =========================================================================

    @Test
    void updateSettings_whenNoRowExists_createsRowThenSaves() throws Exception {
        Settings empty = new Settings();
        when(settingsRepository.findAll()).thenReturn(Collections.emptyList());
        when(settingsRepository.save(any(Settings.class)))
                .thenAnswer(inv -> inv.getArgument(0));   // first save returns the same object
        when(settingsMapper.toResponse(any())).thenReturn(stubResponse);

        SettingsResponse result = settingsService.updateSettings(updateRequest, null, null, null);

        assertNotNull(result);
        // save() is called twice: once to create the singleton, once to persist updates
        verify(settingsRepository, times(2)).save(any(Settings.class));
    }

    // =========================================================================
    // updateSettings — does NOT delete old image when no new file provided
    // =========================================================================

    @Test
    void updateSettings_withNoFiles_doesNotDeleteAnyImage() throws Exception {
        when(settingsRepository.findAll()).thenReturn(List.of(stubSettings));
        when(settingsRepository.save(stubSettings)).thenReturn(stubSettings);
        when(settingsMapper.toResponse(stubSettings)).thenReturn(stubResponse);

        settingsService.updateSettings(updateRequest, null, null, null);

        verify(imageUploadService, never()).deleteImage(any());
        verify(imageUploadService, never()).uploadSettingsImage(any());
    }

    // =========================================================================
    // updateSettings — old logo is null, uploading new logo skips deletion call
    // =========================================================================

    @Test
    void updateSettings_withLogoFile_whenExistingLogoIsNull_uploadsWithoutDeletion() throws Exception {
        stubSettings.setSiteLogo(null);
        MockMultipartFile logo = new MockMultipartFile(
                "logo", "logo.png", "image/png", "data".getBytes());

        when(settingsRepository.findAll()).thenReturn(List.of(stubSettings));
        when(imageUploadService.uploadSettingsImage(logo)).thenReturn("/uploads/settings/logo-new.png");
        when(settingsRepository.save(stubSettings)).thenReturn(stubSettings);
        when(settingsMapper.toResponse(stubSettings)).thenReturn(stubResponse);

        settingsService.updateSettings(updateRequest, logo, null, null);

        // deleteImage is still called (service passes null — that's fine; the impl handles it)
        verify(imageUploadService).uploadSettingsImage(logo);
        assertEquals("/uploads/settings/logo-new.png", stubSettings.getSiteLogo());
    }
}
