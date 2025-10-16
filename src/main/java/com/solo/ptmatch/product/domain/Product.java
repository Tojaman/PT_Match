package com.solo.ptmatch.product.domain;

import com.solo.ptmatch.common.BaseEntity;
import com.solo.ptmatch.trainer.domain.TrainerProfile;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "products")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product extends BaseEntity {

    private static final BigDecimal ZERO_PRICE = BigDecimal.ZERO.setScale(2, RoundingMode.UNNECESSARY);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_profile_id", nullable = false)
    private TrainerProfile trainerProfile;

    @Column(nullable = false)
    private String title;

    @Lob
    @Column(nullable = false)
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ProductCategory category;

    @Column(name = "price_per_session", nullable = false, precision = 10, scale = 2)
    private BigDecimal pricePerSession;

    @Column(name = "session_count", nullable = false)
    private int sessionCount;

    @Column(name = "likes_count", nullable = false)
    private int likesCount;

    @Enumerated(EnumType.STRING)
    @Column(name = "sale_status", nullable = false)
    private ProductSaleStatus saleStatus;

    private Product(
            TrainerProfile trainerProfile,
            String title,
            String description,
            ProductCategory category,
            BigDecimal pricePerSession,
            int sessionCount
    ) {
        this.trainerProfile = trainerProfile;
        this.title = title;
        this.description = description;
        this.category = category;
        this.pricePerSession = sanitizePrice(pricePerSession);
        this.sessionCount = sessionCount;
        this.likesCount = 0;
        this.saleStatus = ProductSaleStatus.ACTIVE;
    }

    public static Product create(
            TrainerProfile trainerProfile,
            String title,
            String description,
            ProductCategory category,
            BigDecimal pricePerSession,
            int sessionCount
    ) {
        return new Product(trainerProfile, title, description, category, pricePerSession, sessionCount);
    }

    public void update(
            String title,
            String description,
            ProductCategory category,
            BigDecimal pricePerSession,
            int sessionCount
    ) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.pricePerSession = sanitizePrice(pricePerSession);
        this.sessionCount = sessionCount;
    }

    public void deactivate() {
        this.saleStatus = ProductSaleStatus.INACTIVE;
    }

    private BigDecimal sanitizePrice(BigDecimal price) {
        BigDecimal validated = price;
        if (validated.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("price must not be negative");
        }
        return validated.setScale(2, RoundingMode.HALF_UP);
    }
}
