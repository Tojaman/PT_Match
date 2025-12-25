package com.solo.ptmatch.matching.domain;

import com.solo.ptmatch.common.BaseEntity;
import com.solo.ptmatch.trainer.domain.AvailableSchedule;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "matching_schedules")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MatchingSchedule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "matching_schedule_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matching_id", nullable = false)
    private Matching matching;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "available_schedule_id", nullable = false, unique = true)
    private AvailableSchedule availableSchedule;

    @Enumerated(EnumType.STRING)
    @Column(name = "session_status", nullable = false)
    private SessionStatus sessionStatus;

    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    private MatchingSchedule(AvailableSchedule availableSchedule,
            LocalDateTime startTime,
            LocalDateTime endTime,
            SessionStatus sessionStatus) {
        this.availableSchedule = availableSchedule;
        this.startTime = startTime;
        this.endTime = endTime;
        this.sessionStatus = sessionStatus;
    }

    public static MatchingSchedule from(AvailableSchedule availableSchedule) {
        return new MatchingSchedule(
                availableSchedule,
                availableSchedule.getStartTime(),
                availableSchedule.getEndTime(),
                SessionStatus.SCHEDULED);
    }

    void assignMatching(Matching matching) {
        this.matching = matching;
    }

    public void complete() {
        this.sessionStatus = SessionStatus.COMPLETED;
    }
}
