package com.asal.ecommerce.controller.admin;

import com.asal.ecommerce.dto.SiteImageOrderRequest;
import com.asal.ecommerce.dto.SiteImageResponse;
import com.asal.ecommerce.service.SiteImageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockMultipartFile;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AdminSiteImageControllerTest {

    @Mock
    private SiteImageService siteImageService;

    @InjectMocks
    private SiteImageController siteImageController;

    private SiteImageResponse stubResponse;
    private MockMultipartFile mockFile;

    @BeforeEach
    void setUp() {
        stubResponse = new SiteImageResponse();
        stubResponse.setId(1L);
        stubResponse.setImageUrl("/uploads/site-images/banner1.jpg");
        stubResponse.setDisplayOrder(0);
        stubResponse.setCreatedAt(LocalDateTime.now());

        mockFile = new MockMultipartFile(
                "image", "banner.jpg", "image/jpeg", "fake-image-data".getBytes());
    }

    // =========================================================================
    // GET /api/admin/site-images
    // =========================================================================

    @Test
    void getAllSiteImages_returnsOkWithList() {
        when(siteImageService.getAllSiteImages()).thenReturn(List.of(stubResponse));

        ResponseEntity<List<SiteImageResponse>> result = siteImageController.getAllSiteImages();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1, result.getBody().size());
        assertEquals(1L, result.getBody().get(0).getId());
        verify(siteImageService).getAllSiteImages();
    }

    @Test
    void getAllSiteImages_whenEmpty_returnsOkWithEmptyList() {
        when(siteImageService.getAllSiteImages()).thenReturn(Collections.emptyList());

        ResponseEntity<List<SiteImageResponse>> result = siteImageController.getAllSiteImages();

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertTrue(result.getBody().isEmpty());
    }

    @Test
    void getAllSiteImages_propagatesRuntimeException() {
        when(siteImageService.getAllSiteImages()).thenThrow(new RuntimeException("DB error"));

        assertThrows(RuntimeException.class, () -> siteImageController.getAllSiteImages());
    }

    // =========================================================================
    // POST /api/admin/site-images
    // =========================================================================

    @Test
    void addSiteImage_success_returnsCreated() throws Exception {
        when(siteImageService.addSiteImage(mockFile, 0)).thenReturn(stubResponse);

        ResponseEntity<SiteImageResponse> result =
                siteImageController.addSiteImage(mockFile, 0);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(1L, result.getBody().getId());
        verify(siteImageService).addSiteImage(mockFile, 0);
    }

    @Test
    void addSiteImage_withCustomOrder_delegatesOrder() throws Exception {
        stubResponse.setDisplayOrder(3);
        when(siteImageService.addSiteImage(mockFile, 3)).thenReturn(stubResponse);

        ResponseEntity<SiteImageResponse> result =
                siteImageController.addSiteImage(mockFile, 3);

        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertEquals(3, result.getBody().getDisplayOrder());
        verify(siteImageService).addSiteImage(mockFile, 3);
    }

    @Test
    void addSiteImage_whenIllegalArgument_returnsBadRequest() throws Exception {
        when(siteImageService.addSiteImage(any(), any()))
                .thenThrow(new IllegalArgumentException("Not an image"));

        ResponseEntity<SiteImageResponse> result =
                siteImageController.addSiteImage(mockFile, 0);

        assertEquals(HttpStatus.BAD_REQUEST, result.getStatusCode());
    }

    @Test
    void addSiteImage_whenUnexpectedException_returnsInternalServerError() throws Exception {
        when(siteImageService.addSiteImage(any(), any()))
                .thenThrow(new RuntimeException("IO failure"));

        ResponseEntity<SiteImageResponse> result =
                siteImageController.addSiteImage(mockFile, 0);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, result.getStatusCode());
    }

    // =========================================================================
    // PATCH /api/admin/site-images/{id}/order
    // =========================================================================

    @Test
    void updateDisplayOrder_found_returnsOkWithUpdatedResponse() {
        SiteImageOrderRequest orderReq = new SiteImageOrderRequest();
        orderReq.setDisplayOrder(5);

        SiteImageResponse updated = new SiteImageResponse();
        updated.setId(1L);
        updated.setDisplayOrder(5);

        when(siteImageService.updateDisplayOrder(1L, 5)).thenReturn(updated);

        ResponseEntity<SiteImageResponse> result =
                siteImageController.updateDisplayOrder(1L, orderReq);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(5, result.getBody().getDisplayOrder());
        verify(siteImageService).updateDisplayOrder(1L, 5);
    }

    @Test
    void updateDisplayOrder_notFound_propagatesRuntimeException() {
        SiteImageOrderRequest orderReq = new SiteImageOrderRequest();
        orderReq.setDisplayOrder(2);

        when(siteImageService.updateDisplayOrder(99L, 2))
                .thenThrow(new RuntimeException("Site image not found with id: 99"));

        assertThrows(RuntimeException.class,
                () -> siteImageController.updateDisplayOrder(99L, orderReq));
    }

    // =========================================================================
    // DELETE /api/admin/site-images/{id}
    // =========================================================================

    @Test
    void deleteSiteImage_found_returnsNoContent() {
        doNothing().when(siteImageService).deleteSiteImage(1L);

        ResponseEntity<Void> result = siteImageController.deleteSiteImage(1L);

        assertEquals(HttpStatus.NO_CONTENT, result.getStatusCode());
        assertNull(result.getBody());
        verify(siteImageService).deleteSiteImage(1L);
    }

    @Test
    void deleteSiteImage_notFound_propagatesRuntimeException() {
        doThrow(new RuntimeException("Site image not found with id: 99"))
                .when(siteImageService).deleteSiteImage(99L);

        assertThrows(RuntimeException.class,
                () -> siteImageController.deleteSiteImage(99L));

        verify(siteImageService).deleteSiteImage(99L);
    }
}
