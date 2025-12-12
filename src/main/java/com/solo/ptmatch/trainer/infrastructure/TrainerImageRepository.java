package com.solo.ptmatch.trainer.infrastructure;

import com.solo.ptmatch.trainer.domain.TrainerImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TrainerImageRepository extends JpaRepository<TrainerImage, Long> {
    List<TrainerImage> findAllByTrainerProfileId(Long trainerProfileId);
}
