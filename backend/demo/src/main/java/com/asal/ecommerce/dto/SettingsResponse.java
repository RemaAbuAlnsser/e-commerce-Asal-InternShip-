package com.asal.ecommerce.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class SettingsResponse {

    private Long id;
    private String siteLogo;
    private String siteFavicon;
    private String siteImage;
    private String siteName;
    private String siteDescription;
    private String contactEmail;
    private String contactPhone;
    private String address;
    private String facebookUrl;
    private String instagramUrl;
    private String whatsappUrl;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
