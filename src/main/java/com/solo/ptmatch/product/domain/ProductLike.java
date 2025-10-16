package com.solo.ptmatch.product.domain;

import com.solo.ptmatch.common.BaseEntity;
import com.solo.ptmatch.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "product_likes")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProductLike extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "product_like_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "product_id")
    private Product product;

    private ProductLike(User user, Product product) {
        this.user = user;
        this.product = product;
        validateDifferentAccounts(user, product);
    }

    public static ProductLike create(User user, Product product) {
        return new ProductLike(user, product);
    }

    private void validateDifferentAccounts(User user, Product product) {
        if (product.getTrainerProfile().getUser().equals(user)) {
            throw new IllegalArgumentException("trainer cannot like their own product");
        }
    }
}
