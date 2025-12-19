package com.solo.ptmatch.matching.infrastructure;

import com.solo.ptmatch.trainer.domain.AvailableSchedule;
import com.solo.ptmatch.trainer.domain.ReservationStatus;
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

    public List<AvailableSchedule> findAllByTrainerProfileIdAndReservationStatus(Long trainerId,
            ReservationStatus reservationStatus);

    @Query("SELECT COUNT(s) FROM AvailableSchedule s " +
            "WHERE s.trainerProfile.id = :trainerId " +
            "AND s.startTime < :endTime " +
            "AND s.endTime > :startTime")
    Long countOverlappingSchedule(Long trainerId, LocalDateTime startTime, LocalDateTime endTime);
}
