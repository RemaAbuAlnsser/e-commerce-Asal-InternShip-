package com.asal.ecommerce.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "order_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id", nullable = false)
    private Order order;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_color_id", nullable = true)
    private ProductColor productColor;

    // Snapshot at order time
    @Column(name = "product_name", nullable = false, length = 191)
    private String productName;

    @Column(name = "product_price", nullable = false, precision = 10, scale = 2)
    private BigDecimal productPrice;

    @Column(name = "color_name", length = 100)
    private String colorName;

    @Column(name = "quantity", nullable = false)
    private int quantity = 1;

    @Column(name = "subtotal", nullable = false, precision = 10, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    public void onCreate() {
        this.createdAt = LocalDateTime.now();
    }
}
