package com.solo.ptmatch.matching.domain;

import com.solo.ptmatch.trainer.domain.TrainerProfile;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
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
public class AvailableSchedule {

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

    @Column(name = "is_booked", nullable = false)
    private boolean booked;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private AvailableSchedule(TrainerProfile trainerProfile, LocalDateTime startTime, LocalDateTime endTime) {
        this.trainerProfile = Objects.requireNonNull(trainerProfile, "trainerProfile must not be null");
        this.startTime = Objects.requireNonNull(startTime, "startTime must not be null");
        this.endTime = Objects.requireNonNull(endTime, "endTime must not be null");
        validateTimeRange();
    }

    public static AvailableSchedule create(TrainerProfile trainerProfile, LocalDateTime startTime, LocalDateTime endTime) {
        return new AvailableSchedule(trainerProfile, startTime, endTime);
    }

    public void markBooked() {
        if (booked) {
            throw new IllegalStateException("Schedule is already booked");
        }
        booked = true;
    }

    public void release() {
        if (!booked) {
            return;
        }
        booked = false;
    }

    public boolean isAvailable(LocalDateTime at) {
        Objects.requireNonNull(at, "at must not be null");
        return !booked && !at.isBefore(startTime) && at.isBefore(endTime);
    }

    private void validateTimeRange() {
        if (!startTime.isBefore(endTime)) {
            throw new IllegalArgumentException("startTime must be before endTime");
        }
    }

    @PrePersist
    private void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
        booked = false;
    }

    @PreUpdate
    private void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
