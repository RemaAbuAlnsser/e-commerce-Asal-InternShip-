package com.asal.ecommerce.model;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "cart_items")
@Data
@NoArgsConstructor
public class CartItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "product_id", nullable = false)
    private Long productId;

    @Column(name = "product_name", nullable = false)
    private String productName;

    @Column(name = "product_image")
    private String productImage;

    @Column(name = "category_name")
    private String categoryName;

    @Column(nullable = false)
    private double price;

    @Column(name = "old_price")
    private Double oldPrice;

    @Column(name = "color_id")
    private Integer colorId;

    @Column(name = "color_name")
    private String colorName;

    @Column(name = "color_hex")
    private String colorHex;

    @Column(nullable = false)
    private int quantity = 1;

    @Column(name = "max_stock", nullable = false)
    private int maxStock;
}
