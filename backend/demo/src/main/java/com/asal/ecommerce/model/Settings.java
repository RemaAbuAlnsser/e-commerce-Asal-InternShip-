package com.asal.ecommerce.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "settings")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Settings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "site_logo", length = 500)
    private String siteLogo;

    @Column(name = "site_favicon", length = 500)
    private String siteFavicon;

    @Column(name = "site_image", length = 500)
    private String siteImage;

    @Column(name = "site_name", length = 191)
    private String siteName;

    @Column(name = "site_description", columnDefinition = "TEXT")
    private String siteDescription;

    @Column(name = "contact_email", length = 191)
    private String contactEmail;

    @Column(name = "contact_phone", length = 50)
    private String contactPhone;

    @Column(name = "address", columnDefinition = "TEXT")
    private String address;

    @Column(name = "facebook_url", length = 500)
    private String facebookUrl;

    @Column(name = "instagram_url", length = 500)
    private String instagramUrl;

    @Column(name = "whatsapp_url", length = 500)
    private String whatsappUrl;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
}
