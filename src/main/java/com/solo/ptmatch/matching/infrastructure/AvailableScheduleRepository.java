package com.solo.ptmatch.matching.infrastructure;

import com.solo.ptmatch.trainer.domain.AvailableSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AvailableScheduleRepository extends JpaRepository<AvailableSchedule, Long> {

    public AvailableSchedule findByIdAndTrainerProfileId(Long id, Long trainerId);
}
