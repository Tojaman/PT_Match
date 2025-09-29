package com.solo.ptmatch.product.domain;

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
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "products")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {

    private static final BigDecimal ZERO_PRICE = BigDecimal.ZERO.setScale(2, RoundingMode.UNNECESSARY);

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_profile_id", nullable = false)
    private TrainerProfile trainerProfile;

    @Column(nullable = false)
    private String name;

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

    @Column(name = "thumbnail_url")
    private String thumbnailUrl;

    @Column(name = "likes_count", nullable = false)
    private int likesCount;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private Product(
            TrainerProfile trainerProfile,
            String name,
            String description,
            ProductCategory category,
            BigDecimal pricePerSession,
            int sessionCount,
            String thumbnailUrl
    ) {
        this.trainerProfile = Objects.requireNonNull(trainerProfile, "trainerProfile must not be null");
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.description = Objects.requireNonNull(description, "description must not be null");
        this.category = Objects.requireNonNull(category, "category must not be null");
        this.pricePerSession = sanitizePrice(pricePerSession);
        this.sessionCount = validateSessionCount(sessionCount);
        this.thumbnailUrl = thumbnailUrl;
    }

    public static Product create(
            TrainerProfile trainerProfile,
            String name,
            String description,
            ProductCategory category,
            BigDecimal pricePerSession,
            int sessionCount,
            String thumbnailUrl
    ) {
        return new Product(trainerProfile, name, description, category, pricePerSession, sessionCount, thumbnailUrl);
    }

    public void updateDetails(
            String name,
            String description,
            ProductCategory category,
            BigDecimal pricePerSession,
            int sessionCount,
            String thumbnailUrl
    ) {
        this.name = Objects.requireNonNull(name, "name must not be null");
        this.description = Objects.requireNonNull(description, "description must not be null");
        this.category = Objects.requireNonNull(category, "category must not be null");
        this.pricePerSession = sanitizePrice(pricePerSession);
        this.sessionCount = validateSessionCount(sessionCount);
        this.thumbnailUrl = thumbnailUrl;
    }

    public void toggleLike(boolean liked) {
        if (liked) {
            likesCount += 1;
            return;
        }
        if (likesCount == 0) {
            return;
        }
        likesCount -= 1;
    }

    private BigDecimal sanitizePrice(BigDecimal price) {
        BigDecimal validated = Objects.requireNonNull(price, "price must not be null");
        if (validated.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("price must not be negative");
        }
        return validated.setScale(2, RoundingMode.HALF_UP);
    }

    private int validateSessionCount(int sessionCount) {
        if (sessionCount <= 0) {
            throw new IllegalArgumentException("sessionCount must be positive");
        }
        return sessionCount;
    }

    @PrePersist
    private void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        likesCount = 0;
        if (pricePerSession == null) {
            pricePerSession = ZERO_PRICE;
        }
    }

    @PreUpdate
    private void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
