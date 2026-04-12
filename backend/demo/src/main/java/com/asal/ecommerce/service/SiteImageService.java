package com.asal.ecommerce.service;

import com.asal.ecommerce.dto.SiteImageResponse;
import com.asal.ecommerce.mapper.SiteImageMapper;
import com.asal.ecommerce.model.SiteImage;
import com.asal.ecommerce.repository.SiteImageRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class SiteImageService {

    @Autowired
    private SiteImageRepository siteImageRepository;

    @Autowired
    private SiteImageMapper siteImageMapper;

    @Autowired
    private ImageUploadService imageUploadService;

    @Transactional(readOnly = true)
    public List<SiteImageResponse> getAllSiteImages() {
        return siteImageRepository.findAllByOrderByDisplayOrderAsc()
                .stream()
                .map(siteImageMapper::toResponse)
                .collect(Collectors.toList());
    }

    public SiteImageResponse addSiteImage(MultipartFile imageFile, Integer displayOrder) throws Exception {
        String imageUrl = imageUploadService.uploadSiteImage(imageFile);

        SiteImage siteImage = new SiteImage();
        siteImage.setImageUrl(imageUrl);
        siteImage.setDisplayOrder(displayOrder != null ? displayOrder : 0);

        SiteImage saved = siteImageRepository.save(siteImage);
        return siteImageMapper.toResponse(saved);
    }

    public SiteImageResponse updateDisplayOrder(Long id, Integer displayOrder) {
        SiteImage siteImage = siteImageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Site image not found with id: " + id));

        siteImage.setDisplayOrder(displayOrder);
        SiteImage saved = siteImageRepository.save(siteImage);
        return siteImageMapper.toResponse(saved);
    }

    public void deleteSiteImage(Long id) {
        SiteImage siteImage = siteImageRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Site image not found with id: " + id));

        imageUploadService.deleteImage(siteImage.getImageUrl());
        siteImageRepository.delete(siteImage);
    }
}
