package com.asal.ecommerce.model;

import jakarta.persistence.*;
import lombok.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "product_colors")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ProductColor {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "color_name", nullable = false)
    private String colorName;

    @Column(name = "color_hex", nullable = false)
    private String colorHex;

    @Column(nullable = false)
    private int stock = 0;

    @OneToMany(mappedBy = "productColor", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<ProductColorImage> images = new ArrayList<>();
}