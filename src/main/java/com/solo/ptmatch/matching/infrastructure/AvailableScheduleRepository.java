package com.solo.ptmatch.matching.infrastructure;

import com.solo.ptmatch.trainer.domain.AvailableSchedule;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AvailableScheduleRepository extends JpaRepository<AvailableSchedule, Long> {

    public AvailableSchedule findByIdAndTrainerProfileId(Long id, Long trainerId);

    public List<AvailableSchedule> findAllByIdInAndTrainerProfileId(List<Long> ids, Long trainerId);

    public List<AvailableSchedule> findAllByTrainerProfileId(Long trainerId);
}
