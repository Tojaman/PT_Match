package com.solo.ptmatch.trainer.domain;

import com.solo.ptmatch.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "available_schedules")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AvailableSchedule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "available_schedule_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_profile_id", nullable = false)
    private TrainerProfile trainerProfile;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    @Column(name = "reservation_status", nullable = false)
    private ReservationStatus reservationStatus;

    private AvailableSchedule(TrainerProfile trainerProfile, LocalDateTime startTime, LocalDateTime endTime) {
        this.trainerProfile = trainerProfile;
        this.startTime = startTime;
        this.endTime = endTime;
        validateTimeRange();
    }

    public static AvailableSchedule create(TrainerProfile trainerProfile, LocalDateTime startTime, LocalDateTime endTime) {
        return new AvailableSchedule(trainerProfile, startTime, endTime);
    }

    public void update(LocalDateTime startTime, LocalDateTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }

    private void validateTimeRange() {
        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("startTime must be before endTime");
        }
    }
}
