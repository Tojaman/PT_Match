package com.solo.ptmatch.trainer.infrastructure;

import com.solo.ptmatch.trainer.domain.TrainerFollow;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainerFollowRepository extends JpaRepository<TrainerFollow, Long> {
}
