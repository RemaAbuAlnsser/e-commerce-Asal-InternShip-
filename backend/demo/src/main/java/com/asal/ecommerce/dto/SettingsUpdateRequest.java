package com.asal.ecommerce.dto;

import lombok.Data;

@Data
public class SettingsUpdateRequest {

    private String siteName;
    private String siteDescription;
    private String contactEmail;
    private String contactPhone;
    private String address;
    private String facebookUrl;
    private String instagramUrl;
    private String whatsappUrl;
}
