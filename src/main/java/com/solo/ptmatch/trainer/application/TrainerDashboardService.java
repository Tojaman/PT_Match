package com.solo.ptmatch.trainer.application;

import com.solo.ptmatch.common.exception.ErrorCode;
import com.solo.ptmatch.common.exception.GlobalException;
import com.solo.ptmatch.matching.infrastructure.MatchingRepository;
import com.solo.ptmatch.matching.infrastructure.MatchingScheduleRepository;
import com.solo.ptmatch.trainer.domain.TrainerProfile;
import com.solo.ptmatch.trainer.infrastructure.TrainerProfileRepository;
import com.solo.ptmatch.trainer.presentation.response.TrainerDashboardStatsResponse;
import com.solo.ptmatch.user.domain.User;
import com.solo.ptmatch.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@RequiredArgsConstructor
@Service
public class TrainerDashboardService {

    private final UserRepository userRepository;
    private final TrainerProfileRepository trainerProfileRepository;
    private final MatchingScheduleRepository matchingScheduleRepository;
    private final MatchingRepository matchingRepository;

    @Transactional(readOnly = true)
    public TrainerDashboardStatsResponse getDashboardStats(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> GlobalException.of(ErrorCode.USER_NOT_FOUND));

        TrainerProfile profile = trainerProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> GlobalException.of(ErrorCode.TRAINER_PROFILE_NOT_FOUND));

        // 오늘 날짜 범위 계산
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = LocalDate.now().atTime(LocalTime.MAX);

        // 통계 계산
        int todaySessions = matchingScheduleRepository.countTodaySessionsByTrainerProfileId(
                profile.getId(), startOfDay, endOfDay);

        int activeMembers = matchingRepository.countActiveMembersByTrainerProfileId(profile.getId());
        int pendingRequests = matchingRepository.countPendingRequestsByTrainerProfileId(profile.getId());

        return TrainerDashboardStatsResponse.of(todaySessions, activeMembers, pendingRequests);
    }
}
