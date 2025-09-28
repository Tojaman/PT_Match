package com.solo.ptmatch.trainer.infrastructure;

import com.solo.ptmatch.trainer.domain.TrainerProfile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainerProfileRepository extends JpaRepository<TrainerProfile, Long> {
}
