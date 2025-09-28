package com.solo.ptmatch.review.infrastructure;

import com.solo.ptmatch.review.domain.Review;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ReviewRepository extends JpaRepository<Review, Long> {
}
