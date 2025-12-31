package com.solo.ptmatch.statistics.application;

import com.solo.ptmatch.matching.infrastructure.MatchingRepository;
import com.solo.ptmatch.matching.infrastructure.MatchingScheduleRepository;
import com.solo.ptmatch.review.infrastructure.ReviewRepository;
import com.solo.ptmatch.statistics.domain.TrainerMonthlyStats;
import com.solo.ptmatch.statistics.infrastructure.TrainerMonthlyStatsRepository;
import com.solo.ptmatch.statistics.presentation.response.*;
import com.solo.ptmatch.trainer.domain.TrainerProfile;
import com.solo.ptmatch.trainer.infrastructure.TrainerProfileRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class TrainerMonthlyStatsService {

    private final TrainerMonthlyStatsRepository statsRepository;
    private final TrainerProfileRepository trainerProfileRepository;
    private final MatchingRepository matchingRepository;
    private final MatchingScheduleRepository scheduleRepository;
    private final ReviewRepository reviewRepository;
    private final ThreadPoolTaskExecutor statsExecutor;

    public TrainerMonthlyStatsService(
            TrainerMonthlyStatsRepository statsRepository,
            TrainerProfileRepository trainerProfileRepository,
            MatchingRepository matchingRepository,
            MatchingScheduleRepository scheduleRepository,
            ReviewRepository reviewRepository,
            @Qualifier("statsExecutor") ThreadPoolTaskExecutor statsExecutor) {
        this.statsRepository = statsRepository;
        this.trainerProfileRepository = trainerProfileRepository;
        this.matchingRepository = matchingRepository;
        this.scheduleRepository = scheduleRepository;
        this.reviewRepository = reviewRepository;
        this.statsExecutor = statsExecutor;
    }

    // 특정 트레이너의 특정 월 통계 집계
    @Transactional
    public void aggregateMonthlyStats(Long trainerProfileId, int year, int month) {
        TrainerProfile trainerProfile = trainerProfileRepository.findById(trainerProfileId)
                .orElseThrow(() -> new IllegalArgumentException(
                        "Trainer not found: " + trainerProfileId));

        LocalDateTime start = LocalDateTime.of(year, month, 1, 0, 0);
        LocalDateTime end = start.plusMonths(1);

        // 1. 회원 통계
        int activeMembers = matchingRepository.countActiveMembersByTrainerProfileIdAndDateRange(
                trainerProfileId, start, end);
        int newMembers = matchingRepository.countFirstTimeMembers(trainerProfileId, start, end);
        int reRegistered = matchingRepository.countReRegisteredMembers(trainerProfileId, start, end);

        // 2. 수업 통계
        int completedSessions = scheduleRepository.countCompletedSessionsByTrainerProfileIdAndDateRange(
                trainerProfileId, start, end);
        int scheduledSessions = scheduleRepository.countScheduledSessionsByTrainerProfileIdAndDateRange(
                trainerProfileId, start, end);

        // 3. 매출 계산 (완료된 세션 × 세션당 가격)
        Long revenueValue = scheduleRepository.calculateMonthlyRevenue(trainerProfileId, start, end);
        BigDecimal totalRevenue = BigDecimal.valueOf(revenueValue != null ? revenueValue : 0);

        // 4. 리뷰 통계
        int reviewCount = reviewRepository.countByTrainerProfileIdAndDateRange(trainerProfileId, start, end);
        Double avgRating = reviewRepository.getAverageRatingByTrainerProfileIdAndDateRange(trainerProfileId, start,
                end);
        BigDecimal averageRating = BigDecimal.valueOf(avgRating != null ? avgRating : 0)
                .setScale(2, RoundingMode.HALF_UP);

        // Upsert: 기존 데이터가 있으면 업데이트, 없으면 생성
        TrainerMonthlyStats stats = statsRepository.findByTrainerProfileIdAndYearAndMonth(trainerProfileId, year, month)
                .orElseGet(() -> TrainerMonthlyStats.create(trainerProfile, year, month));

        stats.updateStats(
                activeMembers,
                newMembers,
                reRegistered,
                completedSessions,
                scheduledSessions,
                totalRevenue,
                reviewCount,
                averageRating);

        statsRepository.save(stats);

        log.info(
                "Aggregated stats for trainer {} - {}/{}: active={}, new={}, reRegistered={}, completed={}, revenue={}",
                trainerProfileId, year, month, activeMembers, newMembers, reRegistered,
                completedSessions,
                totalRevenue);
    }

    // 모든 트레이너의 특정 월 통계 집계 (병렬 처리)
    public void aggregateAllTrainersMonthlyStats(int year, int month) {
        List<TrainerProfile> trainers = trainerProfileRepository.findAll();
        log.info("===== 월간 통계 집계 시작: {}/{}, 트레이너 {}명 =====", year, month, trainers.size());

        long startTime = System.currentTimeMillis();
        AtomicInteger successCount = new AtomicInteger(0);
        AtomicInteger failCount = new AtomicInteger(0);

        // CompletableFuture로 병렬 처리
        List<CompletableFuture<Void>> futures = trainers.stream()
                .map(trainer -> CompletableFuture.runAsync(() -> {
                    try {
                        aggregateMonthlyStats(trainer.getId(), year, month);
                        successCount.incrementAndGet();
                    } catch (Exception e) {
                        log.error("트레이너 {} 통계 집계 실패: {}", trainer.getId(), e.getMessage());
                        failCount.incrementAndGet();
                    }
                }, statsExecutor))
                .toList();

        // 모든 작업 완료 대기
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();

        long duration = System.currentTimeMillis() - startTime;
        log.info("===== 월간 통계 집계 완료 =====");
        log.info("소요 시간: {}초, 성공: {}건, 실패: {}건", duration / 1000, successCount.get(), failCount.get());
    }

    // 모든 트레이너의 특정 월 통계 집계 (단일 스레드 - 순차 처리)
    public void aggregateAllTrainersMonthlyStatsSingleThread(int year, int month) {
        List<TrainerProfile> trainers = trainerProfileRepository.findAll();
        log.info("Starting monthly stats aggregation for {} trainers - {}/{}", trainers.size(), year, month);

        for (TrainerProfile trainer : trainers) {
            try {
                aggregateMonthlyStats(trainer.getId(), year, month);
            } catch (Exception e) {
                log.error("Failed to aggregate stats for trainer {}: {}", trainer.getId(), e.getMessage());
            }
        }

        log.info("Completed monthly stats aggregation for {}/{}", year, month);
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