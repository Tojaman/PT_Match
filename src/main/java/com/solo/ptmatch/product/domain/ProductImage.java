package com.solo.ptmatch.product.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "product_images")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductImage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_image_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "display_order", nullable = false)
    private int displayOrder;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private ProductImage(Product product, String imageUrl, int displayOrder) {
        this.product = Objects.requireNonNull(product, "product must not be null");
        this.imageUrl = Objects.requireNonNull(imageUrl, "imageUrl must not be null");
        this.displayOrder = validateDisplayOrder(displayOrder);
    }

    public static ProductImage create(Product product, String imageUrl, int displayOrder) {
        return new ProductImage(product, imageUrl, displayOrder);
    }

    public void changeDisplayOrder(int displayOrder) {
        this.displayOrder = validateDisplayOrder(displayOrder);
    }

    public void updateImage(String imageUrl) {
        this.imageUrl = Objects.requireNonNull(imageUrl, "imageUrl must not be null");
    }

    private int validateDisplayOrder(int displayOrder) {
        if (displayOrder < 0) {
            throw new IllegalArgumentException("displayOrder must be zero or positive");
        }
        return displayOrder;
    }

    @PrePersist
    private void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
