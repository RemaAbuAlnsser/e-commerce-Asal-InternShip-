package com.asal.ecommerce.service;

import com.asal.ecommerce.dto.SiteImageResponse;
import com.asal.ecommerce.mapper.SiteImageMapper;
import com.asal.ecommerce.model.SiteImage;
import com.asal.ecommerce.repository.SiteImageRepository;
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
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SiteImageServiceTest {

    @Mock private SiteImageRepository siteImageRepository;
    @Mock private SiteImageMapper     siteImageMapper;
    @Mock private ImageUploadService  imageUploadService;

    @InjectMocks
    private SiteImageService siteImageService;

    private SiteImage stubImage;
    private SiteImageResponse stubResponse;
    private MockMultipartFile mockFile;

    @BeforeEach
    void setUp() {
        stubImage = new SiteImage();
        stubImage.setId(1L);
        stubImage.setImageUrl("/uploads/site-images/banner1.jpg");
        stubImage.setDisplayOrder(0);
        stubImage.setCreatedAt(LocalDateTime.now());

        stubResponse = new SiteImageResponse();
        stubResponse.setId(1L);
        stubResponse.setImageUrl("/uploads/site-images/banner1.jpg");
        stubResponse.setDisplayOrder(0);

        mockFile = new MockMultipartFile(
                "image", "banner.jpg", "image/jpeg", "fake-image-data".getBytes());
    }

    // =========================================================================
    // getAllSiteImages
    // =========================================================================

    @Test
    void getAllSiteImages_returnsOrderedList() {
        SiteImage img2 = new SiteImage();
        img2.setId(2L);
        img2.setImageUrl("/uploads/site-images/banner2.jpg");
        img2.setDisplayOrder(1);

        SiteImageResponse resp2 = new SiteImageResponse();
        resp2.setId(2L);
        resp2.setDisplayOrder(1);

        when(siteImageRepository.findAllByOrderByDisplayOrderAsc())
                .thenReturn(List.of(stubImage, img2));
        when(siteImageMapper.toResponse(stubImage)).thenReturn(stubResponse);
        when(siteImageMapper.toResponse(img2)).thenReturn(resp2);

        List<SiteImageResponse> result = siteImageService.getAllSiteImages();

        assertEquals(2, result.size());
        assertEquals(0, result.get(0).getDisplayOrder());
        assertEquals(1, result.get(1).getDisplayOrder());
        verify(siteImageRepository).findAllByOrderByDisplayOrderAsc();
    }

    @Test
    void getAllSiteImages_whenEmpty_returnsEmptyList() {
        when(siteImageRepository.findAllByOrderByDisplayOrderAsc())
                .thenReturn(Collections.emptyList());

        List<SiteImageResponse> result = siteImageService.getAllSiteImages();

        assertTrue(result.isEmpty());
    }

    // =========================================================================
    // addSiteImage
    // =========================================================================

    @Test
    void addSiteImage_success_uploadsFileAndSavesEntity() throws Exception {
        when(imageUploadService.uploadSiteImage(mockFile))
                .thenReturn("/uploads/site-images/banner.jpg");
        when(siteImageRepository.save(any(SiteImage.class))).thenReturn(stubImage);
        when(siteImageMapper.toResponse(stubImage)).thenReturn(stubResponse);

        SiteImageResponse result = siteImageService.addSiteImage(mockFile, 0);

        assertNotNull(result);
        assertEquals(1L, result.getId());
        verify(imageUploadService).uploadSiteImage(mockFile);
        verify(siteImageRepository).save(any(SiteImage.class));
    }

    @Test
    void addSiteImage_setsDisplayOrderOnEntity() throws Exception {
        when(imageUploadService.uploadSiteImage(mockFile))
                .thenReturn("/uploads/site-images/banner.jpg");
        when(siteImageRepository.save(any(SiteImage.class))).thenAnswer(inv -> {
            SiteImage saved = inv.getArgument(0);
            assertEquals(5, saved.getDisplayOrder());
            return stubImage;
        });
        when(siteImageMapper.toResponse(stubImage)).thenReturn(stubResponse);

        siteImageService.addSiteImage(mockFile, 5);

        verify(siteImageRepository).save(any(SiteImage.class));
    }

    @Test
    void addSiteImage_whenDisplayOrderIsNull_defaultsToZero() throws Exception {
        when(imageUploadService.uploadSiteImage(mockFile))
                .thenReturn("/uploads/site-images/banner.jpg");
        when(siteImageRepository.save(any(SiteImage.class))).thenAnswer(inv -> {
            SiteImage saved = inv.getArgument(0);
            assertEquals(0, saved.getDisplayOrder());
            return stubImage;
        });
        when(siteImageMapper.toResponse(stubImage)).thenReturn(stubResponse);

        siteImageService.addSiteImage(mockFile, null);

        verify(siteImageRepository).save(any(SiteImage.class));
    }

    @Test
    void addSiteImage_setsImageUrlFromUploadService() throws Exception {
        String expectedUrl = "/uploads/site-images/new-banner.jpg";
        when(imageUploadService.uploadSiteImage(mockFile)).thenReturn(expectedUrl);
        when(siteImageRepository.save(any(SiteImage.class))).thenAnswer(inv -> {
            SiteImage saved = inv.getArgument(0);
            assertEquals(expectedUrl, saved.getImageUrl());
            return stubImage;
        });
        when(siteImageMapper.toResponse(stubImage)).thenReturn(stubResponse);

        siteImageService.addSiteImage(mockFile, 0);

        verify(imageUploadService).uploadSiteImage(mockFile);
    }

    // =========================================================================
    // updateDisplayOrder
    // =========================================================================

    @Test
    void updateDisplayOrder_found_setsNewOrderAndSaves() {
        SiteImageResponse updatedResp = new SiteImageResponse();
        updatedResp.setId(1L);
        updatedResp.setDisplayOrder(3);

        when(siteImageRepository.findById(1L)).thenReturn(Optional.of(stubImage));
        when(siteImageRepository.save(stubImage)).thenReturn(stubImage);
        when(siteImageMapper.toResponse(stubImage)).thenReturn(updatedResp);

        SiteImageResponse result = siteImageService.updateDisplayOrder(1L, 3);

        assertEquals(3, result.getDisplayOrder());
        assertEquals(3, stubImage.getDisplayOrder());
        verify(siteImageRepository).save(stubImage);
    }

    @Test
    void updateDisplayOrder_notFound_throwsRuntimeException() {
        when(siteImageRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> siteImageService.updateDisplayOrder(99L, 1));

        assertEquals("Site image not found with id: 99", ex.getMessage());
        verify(siteImageRepository, never()).save(any());
    }

    // =========================================================================
    // deleteSiteImage
    // =========================================================================

    @Test
    void deleteSiteImage_found_deletesFileAndEntity() {
        when(siteImageRepository.findById(1L)).thenReturn(Optional.of(stubImage));

        siteImageService.deleteSiteImage(1L);

        verify(imageUploadService).deleteImage("/uploads/site-images/banner1.jpg");
        verify(siteImageRepository).delete(stubImage);
    }

    @Test
    void deleteSiteImage_notFound_throwsRuntimeException() {
        when(siteImageRepository.findById(99L)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> siteImageService.deleteSiteImage(99L));

        assertEquals("Site image not found with id: 99", ex.getMessage());
        verify(imageUploadService, never()).deleteImage(any());
        verify(siteImageRepository, never()).delete(any());
    }

    @Test
    void deleteSiteImage_alwaysDeletesPhysicalFileBeforeEntityRemoval() {
        // verify ordering: filesystem removal happens before DB delete
        when(siteImageRepository.findById(1L)).thenReturn(Optional.of(stubImage));

        var order = inOrder(imageUploadService, siteImageRepository);

        siteImageService.deleteSiteImage(1L);

        order.verify(imageUploadService).deleteImage(stubImage.getImageUrl());
        order.verify(siteImageRepository).delete(stubImage);
    }
}
