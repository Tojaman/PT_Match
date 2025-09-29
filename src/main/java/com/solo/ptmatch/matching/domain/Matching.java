package com.solo.ptmatch.matching.domain;

import com.solo.ptmatch.product.domain.Product;
import com.solo.ptmatch.trainer.domain.TrainerProfile;
import com.solo.ptmatch.user.domain.User;
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
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "matching")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Matching {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "matching_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_profile_id", nullable = false)
    private TrainerProfile trainerProfile;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product;

    @Lob
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private MatchingStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private Matching(User user, TrainerProfile trainerProfile, Product product, String message) {
        this.user = Objects.requireNonNull(user, "user must not be null");
        this.trainerProfile = Objects.requireNonNull(trainerProfile, "trainerProfile must not be null");
        this.product = Objects.requireNonNull(product, "product must not be null");
        this.message = message;
        this.status = MatchingStatus.PENDING;
    }

    public static Matching create(User user, TrainerProfile trainerProfile, Product product, String message) {
        return new Matching(user, trainerProfile, product, message);
    }

    public void accept() {
        changeStatus(MatchingStatus.ACCEPTED);
    }

    public void reject() {
        changeStatus(MatchingStatus.REJECTED);
    }

    public void complete() {
        changeStatus(MatchingStatus.COMPLETED);
    }

    public boolean canCreateReservation() {
        return status == MatchingStatus.ACCEPTED;
    }

    private void changeStatus(MatchingStatus targetStatus) {
        ensureTransitionAllowed(targetStatus);
        this.status = targetStatus;
    }

    private void ensureTransitionAllowed(MatchingStatus targetStatus) {
        Objects.requireNonNull(targetStatus, "targetStatus must not be null");
        if (status == targetStatus) {
            return;
        }

        switch (status) {
            case PENDING -> {
                if (targetStatus != MatchingStatus.ACCEPTED && targetStatus != MatchingStatus.REJECTED) {
                    throw new IllegalStateException("Pending matching can only be accepted or rejected");
                }
            }
            case ACCEPTED -> {
                if (targetStatus != MatchingStatus.COMPLETED) {
                    throw new IllegalStateException("Accepted matching can only be completed");
                }
            }
            case REJECTED, COMPLETED -> throw new IllegalStateException("No further transitions allowed for status " + status);
        }
    }

    @PrePersist
    private void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    private void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
