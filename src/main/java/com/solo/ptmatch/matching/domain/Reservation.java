package com.solo.ptmatch.matching.domain;

import com.solo.ptmatch.user.domain.User;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
@Table(name = "reservations")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Reservation {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "reservation_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matching_id", nullable = false)
    private Matching matching;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "available_schedule_id", nullable = false)
    private AvailableSchedule schedule;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ReservationStatus status;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    private Reservation(Matching matching, AvailableSchedule schedule, User user) {
        this.matching = Objects.requireNonNull(matching, "matching must not be null");
        this.schedule = Objects.requireNonNull(schedule, "schedule must not be null");
        this.user = Objects.requireNonNull(user, "user must not be null");
        validateAssociations();
        this.status = ReservationStatus.PENDING_APPROVAL;
    }

    public static Reservation create(Matching matching, AvailableSchedule schedule, User user) {
        return new Reservation(matching, schedule, user);
    }

    public void approve() {
        ensureMatchingAllowsReservation();
        changeStatus(ReservationStatus.SCHEDULED);
        schedule.markBooked();
    }

    public void complete() {
        changeStatus(ReservationStatus.COMPLETED);
    }

    public void cancel() {
        if (status == ReservationStatus.SCHEDULED) {
            schedule.release();
        }
        changeStatus(ReservationStatus.CANCELED);
    }

    public void validateStatusTransition(ReservationStatus targetStatus) {
        Objects.requireNonNull(targetStatus, "targetStatus must not be null");
        if (status == targetStatus) {
            return;
        }
        status.ensureTransitionAllowed(targetStatus);
    }

    private void changeStatus(ReservationStatus targetStatus) {
        validateStatusTransition(targetStatus);
        this.status = targetStatus;
    }

    private void validateAssociations() {
        if (!matching.canCreateReservation()) {
            throw new IllegalStateException("Matching must be accepted to create reservation");
        }
        if (!matching.getTrainerProfile().equals(schedule.getTrainerProfile())) {
            throw new IllegalArgumentException("Schedule must belong to the matching trainer");
        }
        if (!matching.getUser().equals(user)) {
            throw new IllegalArgumentException("Reservation user must match the matching applicant");
        }
    }

    private void ensureMatchingAllowsReservation() {
        if (!matching.canCreateReservation()) {
            throw new IllegalStateException("Matching is not in an accepted state");
        }
        if (schedule.isBooked()) {
            throw new IllegalStateException("Schedule is already booked");
        }
    }

    @PrePersist
    private void onCreate() {
        LocalDateTime now = LocalDateTime.now();
        createdAt = now;
        updatedAt = now;
    }

    @PreUpdate
    private void onUpdate() {
        updatedAt = LocalDateTime.now();
    }
}
