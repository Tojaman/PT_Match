package com.solo.ptmatch.review.infrastructure;

import com.solo.ptmatch.review.domain.Review;
import com.solo.ptmatch.trainer.domain.TrainerProfile;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReviewRepository extends JpaRepository<Review, Long> {
}
