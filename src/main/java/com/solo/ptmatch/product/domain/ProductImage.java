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
import com.solo.ptmatch.common.BaseEntity;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "product_images")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductImage extends BaseEntity {

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

    private ProductImage(Product product, String imageUrl, int displayOrder) {
        this.product = product;
        this.imageUrl = imageUrl;
        this.displayOrder = validateDisplayOrder(displayOrder);
    }

    public static ProductImage create(Product product, String imageUrl, int displayOrder) {
        return new ProductImage(product, imageUrl, displayOrder);
    }

    public void changeDisplayOrder(int displayOrder) {
        this.displayOrder = validateDisplayOrder(displayOrder);
    }

    public void updateImage(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    private int validateDisplayOrder(int displayOrder) {
        if (displayOrder < 0) {
            throw new IllegalArgumentException("displayOrder must be zero or positive");
        }
        return displayOrder;
    }
}
