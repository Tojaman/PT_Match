package com.solo.ptmatch.trainer.infrastructure;

import com.solo.ptmatch.trainer.domain.GymImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GymImageRepository extends JpaRepository<GymImage, Long> {
    List<GymImage> findAllByTrainerProfileId(Long trainerProfileId);
}
