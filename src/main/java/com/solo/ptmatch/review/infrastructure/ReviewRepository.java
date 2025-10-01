package com.solo.ptmatch.review.infrastructure;

import com.solo.ptmatch.review.domain.Review;
import com.solo.ptmatch.trainer.domain.TrainerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
    List<Review> findTop5ByTrainerProfileOrderByCreatedAtDesc(TrainerProfile trainerProfile);

    List<Review> findByProductIdOrderByCreatedAtDesc(Long productId);
}
