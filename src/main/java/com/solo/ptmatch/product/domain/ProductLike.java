package com.solo.ptmatch.product.domain;

import com.solo.ptmatch.user.domain.User;
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
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
        name = "product_likes",
        uniqueConstraints = @UniqueConstraint(columnNames = {"member_id", "product_id"})
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_like_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private User member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    private ProductLike(User member, Product product) {
        this.member = Objects.requireNonNull(member, "member must not be null");
        this.product = Objects.requireNonNull(product, "product must not be null");
    }

    public static ProductLike create(User member, Product product) {
        return new ProductLike(member, product);
    }

    public boolean isOwner(User member) {
        return this.member.equals(Objects.requireNonNull(member, "member must not be null"));
    }

    public boolean isSameProduct(Product product) {
        return this.product.equals(Objects.requireNonNull(product, "product must not be null"));
    }

    public boolean matches(User member, Product product) {
        return isOwner(member) && isSameProduct(product);
    }

    @PrePersist
    private void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
