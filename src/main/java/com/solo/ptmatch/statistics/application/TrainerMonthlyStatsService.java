package com.solo.ptmatch.statistics.application;

import com.solo.ptmatch.matching.infrastructure.MatchingRepository;
import com.solo.ptmatch.matching.infrastructure.MatchingScheduleRepository;
import com.solo.ptmatch.review.infrastructure.ReviewRepository;
import com.solo.ptmatch.statistics.domain.TrainerMonthlyStats;
import com.solo.ptmatch.statistics.infrastructure.TrainerMonthlyStatsRepository;
import com.solo.ptmatch.statistics.presentation.response.*;
import com.solo.ptmatch.trainer.domain.TrainerProfile;
import com.solo.ptmatch.trainer.infrastructure.TrainerProfileRepository;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class TrainerMonthlyStatsService {

    private final TrainerMonthlyStatsRepository statsRepository;
    private final ReviewRepository reviewRepository;

    public TrainerMonthlyStatsService(
            TrainerMonthlyStatsRepository statsRepository,
            ReviewRepository reviewRepository) {
        this.statsRepository = statsRepository;
        this.reviewRepository = reviewRepository;
    }

    /**
     * 대시보드용 데이터 조회
     */
    @Transactional(readOnly = true)
    public MonthlyDashboardResponse getDashboardData(Long trainerProfileId) {
        // 최근 6개월 통계 조회
        List<TrainerMonthlyStats> recentStats = statsRepository.findRecentMonthsByTrainerProfileId(trainerProfileId);

        // 이번 달 통계 (첫 번째)
        TrainerMonthlyStats currentMonth = recentStats.isEmpty() ? null : recentStats.get(0);

        // 전월 통계 (두 번째)
        TrainerMonthlyStats previousMonth = recentStats.size() > 1 ? recentStats.get(1) : null;

        // 별점 분포 조회
        List<Object[]> ratingDistribution = reviewRepository.getRatingDistribution(trainerProfileId);

        // 최근 리뷰 5개 조회
        var recentReviews = reviewRepository.findRecentReviewsByTrainerProfileId(trainerProfileId,
                PageRequest.of(0, 5));

        KpiStats kpiStats = calculateKpiStats(currentMonth, previousMonth);
        MemberDistribution memberDistribution = calculateMemberDistribution(currentMonth);
        ReviewSummary reviewSummary = calculateReviewSummary(ratingDistribution);
        List<MonthlyPerformanceItem> monthlyPerformance = calculateMonthlyPerformance(recentStats);
        List<RecentReviewItem> recentReviewItems = mapRecentReviews(recentReviews);

        return MonthlyDashboardResponse.of(
                kpiStats,
                monthlyPerformance,
                memberDistribution,
                reviewSummary,
                recentReviewItems);
    }

    private KpiStats calculateKpiStats(TrainerMonthlyStats current, TrainerMonthlyStats previous) {
        if (current == null) {
            return KpiStats.empty();
        }

        int activeDiff = previous != null ? current.getActiveMembers() - previous.getActiveMembers() : 0;
        String activeTrend = (activeDiff >= 0 ? "+" : "") + activeDiff;

        String revenueTrend = "0%";
        if (previous != null && previous.getTotalRevenue().compareTo(BigDecimal.ZERO) > 0) {
            BigDecimal diff = current.getTotalRevenue().subtract(previous.getTotalRevenue());
            BigDecimal percent = diff.multiply(BigDecimal.valueOf(100))
                    .divide(previous.getTotalRevenue(), 1, RoundingMode.HALF_UP);
            revenueTrend = (percent.compareTo(BigDecimal.ZERO) >= 0 ? "+" : "") + percent + "%";
        }

        int totalRegistrations = current.getNewMembers() + current.getReRegisteredMembers();
        String registrationRate = totalRegistrations > 0
                ? (current.getReRegisteredMembers() * 100 / totalRegistrations) + "%"
                : "0%";

        return KpiStats.of(
                current.getActiveMembers(),
                activeTrend,
                current.getNewMembers(),
                current.getReRegisteredMembers(),
                registrationRate,
                current.getCompletedSessions(),
                "90%",
                current.getTotalRevenue(),
                revenueTrend);
    }

    private MemberDistribution calculateMemberDistribution(TrainerMonthlyStats current) {
        if (current == null) {
            return MemberDistribution.empty();
        }

        // 해당 월에 활동한 회원 수 (세션이 있는 회원)
        int activeMembers = current.getActiveMembers();
        // 해당 월에 매칭이 생성된 회원 수
        int newMembers = current.getNewMembers();
        int reRegistered = current.getReRegisteredMembers();

        if (activeMembers == 0) {
            return MemberDistribution.empty();
        }

        // 매칭 생성된 총 회원 수
        int totalMatched = newMembers + reRegistered;

        log.info("현재 월 활성 회원 수: {}, 신규 매칭: {}, 재등록 매칭: {}, 총 매칭: {}",
                activeMembers, newMembers, reRegistered, totalMatched);

        int newPercent, reRegisteredPercent, regularPercent;

        if (totalMatched == 0) {
            // 매칭은 없지만 활성 회원이 있는 경우 (기존 회원만 활동)
            newPercent = 0;
            reRegisteredPercent = 0;
            regularPercent = 100;
        } else if (totalMatched <= activeMembers) {
            // 정상 케이스: 매칭된 회원이 모두 활동 중
            // 일반 회원 = 활성 회원 - 신규 - 재등록
            int regularMembers = activeMembers - totalMatched;

            newPercent = newMembers * 100 / activeMembers;
            reRegisteredPercent = reRegistered * 100 / activeMembers;
            regularPercent = regularMembers * 100 / activeMembers;
        } else {
            // 특수 케이스: 매칭은 되었지만 아직 스케줄이 없는 회원이 있음
            // 활성 회원을 신규/재등록 비율로 분배
            int activeNew = activeMembers * newMembers / totalMatched;
            int activeReRegistered = activeMembers * reRegistered / totalMatched;

            newPercent = activeNew * 100 / activeMembers;
            reRegisteredPercent = activeReRegistered * 100 / activeMembers;
            regularPercent = 0; // 모두 신규 또는 재등록

            log.warn("매칭 회원({}) > 활성 회원({}). 비율 기반 분배 적용", totalMatched, activeMembers);
        }

        log.info("신규 회원 비율: {}%, 재등록 회원 비율: {}%, 일반 회원 비율: {}%",
                newPercent, reRegisteredPercent, regularPercent);

        return MemberDistribution.of(
                Math.max(0, regularPercent),
                reRegisteredPercent,
                newPercent);
    }

    private ReviewSummary calculateReviewSummary(List<Object[]> ratingDistribution) {
        Map<Integer, Long> distribution = new HashMap<>();
        for (int i = 1; i <= 5; i++) {
            distribution.put(i, 0L);
        }

        long totalReviews = 0;
        double ratingSum = 0;

        for (Object[] row : ratingDistribution) {
            Integer rating = (Integer) row[0];
            Long count = (Long) row[1];
            distribution.put(rating, count);
            totalReviews += count;
            ratingSum += rating * count;
        }

        BigDecimal averageRating = totalReviews > 0
                ? BigDecimal.valueOf(ratingSum / totalReviews).setScale(1, RoundingMode.HALF_UP)
                : BigDecimal.ZERO;

        return ReviewSummary.of(averageRating, (int) totalReviews, distribution);
    }

    private List<MonthlyPerformanceItem> calculateMonthlyPerformance(List<TrainerMonthlyStats> last6Months) {
        List<MonthlyPerformanceItem> monthlyPerformance = new ArrayList<>(last6Months.stream()
                .map(stats -> MonthlyPerformanceItem.of(
                        stats.getMonth() + "월",
                        stats.getTotalRevenue(),
                        stats.getCompletedSessions()))
                .toList());
        java.util.Collections.reverse(monthlyPerformance);
        return monthlyPerformance;
    }

    private List<RecentReviewItem> mapRecentReviews(List<com.solo.ptmatch.review.domain.Review> recentReviews) {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy.MM.dd");
        return recentReviews.stream()
                .map(r -> RecentReviewItem.of(
                        r.getId(),
                        r.getMatching().getUser().getName(),
                        r.getRating(),
                        r.getContent(),
                        r.getCreatedAt().format(formatter)))
                .toList();
    }
}