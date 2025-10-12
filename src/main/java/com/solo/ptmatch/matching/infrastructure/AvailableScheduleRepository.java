package com.solo.ptmatch.matching.infrastructure;

import com.solo.ptmatch.trainer.domain.AvailableSchedule;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface AvailableScheduleRepository extends JpaRepository<AvailableSchedule, Long> {

    public AvailableSchedule findByIdAndTrainerProfileId(Long id, Long trainerId);

    public List<AvailableSchedule> findAllByIdInAndTrainerProfileId(List<Long> ids, Long trainerId);

    public List<AvailableSchedule> findAllByTrainerProfileId(Long trainerId);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT s FROM AvailableSchedule s WHERE s.id IN :ids")
    public List<AvailableSchedule> findAllByIdInWithLock(List<Long> ids);
}
