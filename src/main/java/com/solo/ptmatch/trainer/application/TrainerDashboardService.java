package com.solo.ptmatch.trainer.application;

import com.solo.ptmatch.common.exception.ErrorCode;
import com.solo.ptmatch.common.exception.GlobalException;
import com.solo.ptmatch.matching.domain.Matching;
import com.solo.ptmatch.matching.domain.MatchingSchedule;
import com.solo.ptmatch.matching.infrastructure.MatchingRepository;
import com.solo.ptmatch.matching.infrastructure.MatchingScheduleRepository;

import com.solo.ptmatch.trainer.domain.TrainerProfile;
import com.solo.ptmatch.trainer.infrastructure.TrainerProfileRepository;
import com.solo.ptmatch.trainer.presentation.response.*;
import com.solo.ptmatch.user.domain.User;
import com.solo.ptmatch.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.YearMonth;
import java.util.List;

@RequiredArgsConstructor
@Service
public class TrainerDashboardService {

    private final UserRepository userRepository;
    private final TrainerProfileRepository trainerProfileRepository;
    private final MatchingScheduleRepository matchingScheduleRepository;
    private final MatchingRepository matchingRepository;

    @Transactional(readOnly = true)
    public TrainerDashboardStatsResponse getDashboardSummary(String email) {
        User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> GlobalException.of(ErrorCode.USER_NOT_FOUND));

        TrainerProfile trainerProfile = trainerProfileRepository.findByUserId(user.getId())
                        .orElseThrow(() -> GlobalException.of(ErrorCode.TRAINER_PROFILE_NOT_FOUND));

        Long trainerProfileId = trainerProfile.getId();
        LocalDate today = LocalDate.now();

        // 오늘 수업 수
        LocalDateTime startOfToday = today.atStartOfDay();
        LocalDateTime endOfToday = today.plusDays(1).atStartOfDay();
        int todaySessions = matchingScheduleRepository.countSessionsByTrainerProfileIdAndDateRange(
                        trainerProfileId, startOfToday, endOfToday);

        // 진행 회원 수
        int activeMembers = matchingRepository.countActiveMembersByTrainerProfileId(trainerProfileId);

        // 신규 요청 (PENDING 상태 매칭 수)
        int pendingRequests = matchingRepository.countPendingRequestsByTrainerProfileId(trainerProfileId);

        // 리뷰 평점
        BigDecimal reviewRating = trainerProfile.getAverageRating();

        return TrainerDashboardStatsResponse.of(todaySessions, activeMembers, pendingRequests, reviewRating);
    }

    @Transactional(readOnly = true)
    public List<MonthlyScheduleResponse> getMonthlyScheduleStatus(String email, int year, int month) {
        User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> GlobalException.of(ErrorCode.USER_NOT_FOUND));

        TrainerProfile trainerProfile = trainerProfileRepository.findByUserId(user.getId())
                        .orElseThrow(() -> GlobalException.of(ErrorCode.TRAINER_PROFILE_NOT_FOUND));

        // 해당 월의 시작일과 종료일 계산
        YearMonth yearMonth = YearMonth.of(year, month);
        LocalDateTime startOfMonth = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = yearMonth.atEndOfMonth().atTime(LocalTime.MAX);

        List<MatchingSchedule> schedules = matchingScheduleRepository
                        .findByTrainerProfileIdAndDateRange(trainerProfile.getId(), startOfMonth, endOfMonth);

        return schedules.stream()
                        .map(MonthlyScheduleResponse::from)
                        .toList();
    }

    @Transactional(readOnly = true)
    public DashboardAnalyticsResponse getDashboardAnalytics(String email) {
        User user = userRepository.findByEmail(email)
                        .orElseThrow(() -> GlobalException.of(ErrorCode.USER_NOT_FOUND));

        TrainerProfile trainerProfile = trainerProfileRepository.findByUserId(user.getId())
                        .orElseThrow(() -> GlobalException.of(ErrorCode.TRAINER_PROFILE_NOT_FOUND));

        Long trainerProfileId = trainerProfile.getId();
        YearMonth currentMonth = YearMonth.now();

        // 월별 수업 횟수 (최근 12개월)
        LocalDateTime startDate = currentMonth.minusMonths(11).atDay(1).atStartOfDay();
        LocalDateTime endDate = currentMonth.atEndOfMonth().atTime(LocalTime.MAX);
        List<MonthlyPerformanceDto> monthlyPerformance = matchingScheduleRepository.countSessionsByMonthForTrainerProfile(trainerProfileId, startDate, endDate);

        // 잔여 횟수 부족 알림 (3회 이하)
        List<Matching> lowSessionMatching = matchingRepository.findLowSessionAlertsByTrainerProfileId(trainerProfileId);
        List<LowSessionAlertDto> lowSessionAlerts = lowSessionMatching.stream()
                        .map(LowSessionAlertDto::from)
                        .toList();

        // 회원 현황
        LocalDateTime thisMonthStart = currentMonth.atDay(1).atStartOfDay();
        LocalDateTime thisMonthEnd = currentMonth.atEndOfMonth().atTime(LocalTime.MAX);
        LocalDateTime lastMonthStart = currentMonth.minusMonths(1).atDay(1).atStartOfDay();
        LocalDateTime lastMonthEnd = currentMonth.minusMonths(1).atEndOfMonth().atTime(LocalTime.MAX);

        int newMembersThisMonth = matchingRepository.countNewMembersByTrainerProfileIdAndDateRange(trainerProfileId, thisMonthStart, thisMonthEnd);
        int newMembersLastMonth = matchingRepository.countNewMembersByTrainerProfileIdAndDateRange(trainerProfileId, lastMonthStart, lastMonthEnd);
        int totalMembers = matchingRepository.countActiveMembersByTrainerProfileId(trainerProfileId);

        MemberStatsDto memberStats = MemberStatsDto.of(newMembersThisMonth, newMembersLastMonth, totalMembers);

        return DashboardAnalyticsResponse.of(monthlyPerformance, lowSessionAlerts, memberStats);
    }
}
