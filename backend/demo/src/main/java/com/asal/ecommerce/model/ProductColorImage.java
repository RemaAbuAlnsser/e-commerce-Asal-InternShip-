package com.asal.ecommerce.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "product_color_images")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder                          // ← this is what was missing
public class ProductColorImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_color_id", nullable = false)
    private ProductColor productColor;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "sort_order", nullable = false)
    private int sortOrder = 0;
}