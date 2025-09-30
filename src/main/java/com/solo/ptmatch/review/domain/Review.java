package com.solo.ptmatch.review.domain;

import com.solo.ptmatch.common.BaseEntity;
import com.solo.ptmatch.trainer.domain.TrainerProfile;
import com.solo.ptmatch.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
@Table(name = "reviews")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseEntity {

    private static final int MIN_RATING = 1;
    private static final int MAX_RATING = 5;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_profile_id", nullable = false)
    private TrainerProfile trainerProfile;

    @Column(nullable = false)
    private int rating;

    @Lob
    @Column(nullable = false)
    private String content;

    private Review(User author, TrainerProfile trainerProfile, int rating, String content) {
        this.author = Objects.requireNonNull(author, "author must not be null");
        this.trainerProfile = Objects.requireNonNull(trainerProfile, "trainerProfile must not be null");
        this.rating = validateRating(rating);
        this.content = Objects.requireNonNull(content, "content must not be null");
    }

    public static Review create(User author, TrainerProfile trainerProfile, int rating, String content) {
        return new Review(author, trainerProfile, rating, content);
    }

    public void updateContent(int rating, String content) {
        this.rating = validateRating(rating);
        this.content = Objects.requireNonNull(content, "content must not be null");
    }

    public boolean isOwner(User user) {
        return this.author.equals(Objects.requireNonNull(user, "user must not be null"));
    }

    private int validateRating(int rating) {
        if (rating < MIN_RATING || rating > MAX_RATING) {
            throw new IllegalArgumentException("rating must be between 1 and 5");
        }
        return rating;
    }
}
