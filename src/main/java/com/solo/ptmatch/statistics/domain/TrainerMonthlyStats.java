package com.solo.ptmatch.statistics.domain;

import com.solo.ptmatch.common.BaseEntity;
import com.solo.ptmatch.trainer.domain.TrainerProfile;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Getter
@Table(name = "trainer_monthly_stats", uniqueConstraints = @UniqueConstraint(columnNames = { "trainer_profile_id",
        "year", "month" }))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class TrainerMonthlyStats extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "stats_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_profile_id", nullable = false)
    private TrainerProfile trainerProfile;

    @Column(nullable = false)
    private Integer year;

    @Column(nullable = false)
    private Integer month;

    // 회원 통계
    @Column(name = "active_members")
    private Integer activeMembers;

    @Column(name = "new_members")
    private Integer newMembers;

    @Column(name = "re_registered_members")
    private Integer reRegisteredMembers;

    // 수업 통계
    @Column(name = "completed_sessions")
    private Integer completedSessions;

    @Column(name = "scheduled_sessions")
    private Integer scheduledSessions;

    // 매출 통계
    @Column(name = "total_revenue", precision = 12, scale = 0)
    private BigDecimal totalRevenue;

    // 리뷰 통계
    @Column(name = "review_count")
    private Integer reviewCount;

    @Column(name = "average_rating", precision = 3, scale = 2)
    private BigDecimal averageRating;

    // 집계 시점
    @Column(name = "last_updated_at")
    private LocalDateTime lastUpdatedAt;

    private TrainerMonthlyStats(TrainerProfile trainerProfile, Integer year, Integer month) {
        this.trainerProfile = trainerProfile;
        this.year = year;
        this.month = month;
        this.activeMembers = 0;
        this.newMembers = 0;
        this.reRegisteredMembers = 0;
        this.completedSessions = 0;
        this.scheduledSessions = 0;
        this.totalRevenue = BigDecimal.ZERO;
        this.reviewCount = 0;
        this.averageRating = BigDecimal.ZERO;
        this.lastUpdatedAt = LocalDateTime.now();
    }

    public static TrainerMonthlyStats create(TrainerProfile trainerProfile, Integer year, Integer month) {
        return new TrainerMonthlyStats(trainerProfile, year, month);
    }

    public void updateStats(
            Integer activeMembers,
            Integer newMembers,
            Integer reRegisteredMembers,
            Integer completedSessions,
            Integer scheduledSessions,
            BigDecimal totalRevenue,
            Integer reviewCount,
            BigDecimal averageRating) {
        this.activeMembers = activeMembers;
        this.newMembers = newMembers;
        this.reRegisteredMembers = reRegisteredMembers;
        this.completedSessions = completedSessions;
        this.scheduledSessions = scheduledSessions;
        this.totalRevenue = totalRevenue;
        this.reviewCount = reviewCount;
        this.averageRating = averageRating;
        this.lastUpdatedAt = LocalDateTime.now();
    }
}
