package com.asal.ecommerce.controller.admin;

import com.asal.ecommerce.dto.SettingsResponse;
import com.asal.ecommerce.dto.SettingsUpdateRequest;
import com.asal.ecommerce.service.SettingsService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminSettingsControllerTest {

    @Mock
    private SettingsService settingsService;

    @InjectMocks
    private SettingsController settingsController;

    private SettingsResponse stubResponse;

    @BeforeEach
    void setUp() {
        stubResponse = new SettingsResponse();
        stubResponse.setId(1L);
        stubResponse.setSiteName("My Store");
        stubResponse.setContactEmail("admin@store.com");
        stubResponse.setContactPhone("+1-555-0000");
    }

    // =========================================================================
    // GET /api/admin/settings
    // =========================================================================

    @Test
    void getSettings_returnsOkWithResponse() {
        when(settingsService.getSettings()).thenReturn(stubResponse);

        ResponseEntity<SettingsResponse> result = settingsController.getSettings();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1L, result.getBody().getId());
        assertEquals("My Store", result.getBody().getSiteName());
        verify(settingsService).getSettings();
    }

    @Test
    void getSettings_propagatesRuntimeException() {
        when(settingsService.getSettings()).thenThrow(new RuntimeException("DB error"));

        assertThrows(RuntimeException.class, () -> settingsController.getSettings());
    }

    // =========================================================================
    // PUT /api/admin/settings — no files
    // =========================================================================

    @Test
    void updateSettings_withTextDataOnly_returnsOk() throws Exception {
        SettingsUpdateRequest request = new SettingsUpdateRequest();
        request.setSiteName("Updated Store");

        when(settingsService.updateSettings(request, null, null, null))
                .thenReturn(stubResponse);

        ResponseEntity<SettingsResponse> result =
                settingsController.updateSettings(request, null, null, null);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        verify(settingsService).updateSettings(request, null, null, null);
    }

    // =========================================================================
    // PUT /api/admin/settings — with logo file
    // =========================================================================

    @Test
    void updateSettings_withLogoFile_returnsOkAndDelegates() throws Exception {
        MockMultipartFile logo = new MockMultipartFile(
                "logo", "logo.png", "image/png", "data".getBytes());
        SettingsUpdateRequest request = new SettingsUpdateRequest();

        when(settingsService.updateSettings(request, logo, null, null))
                .thenReturn(stubResponse);

        ResponseEntity<SettingsResponse> result =
                settingsController.updateSettings(request, logo, null, null);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(settingsService).updateSettings(request, logo, null, null);
    }

    // =========================================================================
    // PUT /api/admin/settings — all three image files
    // =========================================================================

    @Test
    void updateSettings_withAllFiles_returnsOkAndDelegates() throws Exception {
        MockMultipartFile logo    = new MockMultipartFile("logo",      "logo.png", "image/png",    "l".getBytes());
        MockMultipartFile favicon = new MockMultipartFile("favicon",   "fav.ico",  "image/x-icon", "f".getBytes());
        MockMultipartFile cover   = new MockMultipartFile("siteImage", "cover.jpg","image/jpeg",   "c".getBytes());
        SettingsUpdateRequest request = new SettingsUpdateRequest();

        when(settingsService.updateSettings(request, logo, favicon, cover))
                .thenReturn(stubResponse);

        ResponseEntity<SettingsResponse> result =
                settingsController.updateSettings(request, logo, favicon, cover);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(settingsService).updateSettings(request, logo, favicon, cover);
    }

    // =========================================================================
    // PUT /api/admin/settings — null request body defaults to empty object
    // =========================================================================

    @Test
    void updateSettings_whenRequestIsNull_defaultsToEmptyRequest() throws Exception {
        when(settingsService.updateSettings(any(SettingsUpdateRequest.class), isNull(), isNull(), isNull()))
                .thenReturn(stubResponse);

        ResponseEntity<SettingsResponse> result =
                settingsController.updateSettings(null, null, null, null);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        // The controller must have replaced null with a new SettingsUpdateRequest
        verify(settingsService).updateSettings(any(SettingsUpdateRequest.class), isNull(), isNull(), isNull());
    }

    // =========================================================================
    // PUT /api/admin/settings — IllegalArgumentException → 400
    // =========================================================================

    @Test
    void updateSettings_whenIllegalArgument_returnsBadRequest() throws Exception {
        SettingsUpdateRequest request = new SettingsUpdateRequest();
        when(settingsService.updateSettings(any(), any(), any(), any()))
                .thenThrow(new IllegalArgumentException("Invalid image"));

        ResponseEntity<SettingsResponse> result =
                settingsController.updateSettings(request, null, null, null);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    // =========================================================================
    // PUT /api/admin/settings — generic Exception → 500
    // =========================================================================

    @Test
    void updateSettings_whenUnexpectedException_returnsInternalServerError() throws Exception {
        SettingsUpdateRequest request = new SettingsUpdateRequest();
        when(settingsService.updateSettings(any(), any(), any(), any()))
                .thenThrow(new RuntimeException("Disk full"));

        ResponseEntity<SettingsResponse> result =
                settingsController.updateSettings(request, null, null, null);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
    }
}
