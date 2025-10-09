package com.solo.ptmatch.trainer.domain;

import com.solo.ptmatch.common.BaseEntity;
import jakarta.persistence.*;

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

    @Enumerated(EnumType.STRING)
    @Column(name = "reservation_status", nullable = false)
    private ReservationStatus reservationStatus;

    private AvailableSchedule(TrainerProfile trainerProfile,
                              LocalDateTime startTime,
                              LocalDateTime endTime,
                              ReservationStatus reservationStatus) {
        this.trainerProfile = trainerProfile;
        this.startTime = startTime;
        this.endTime = endTime;
        this.reservationStatus = reservationStatus;
    }

    public static AvailableSchedule create(TrainerProfile trainerProfile,
                                           LocalDateTime startTime,
                                           LocalDateTime endTime) {
        return new AvailableSchedule(trainerProfile, startTime, endTime, ReservationStatus.AVAILABLE);
    }

    public void markAsAvailable() {
        this.reservationStatus = ReservationStatus.AVAILABLE;
    }

    public void markAsPending() {
        this.reservationStatus = ReservationStatus.PENDING;
    }

    public void markAsConfirmed() {
        this.reservationStatus = ReservationStatus.CONFIRMED;
    }

    public void update(LocalDateTime startTime,
                       LocalDateTime endTime) {
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
