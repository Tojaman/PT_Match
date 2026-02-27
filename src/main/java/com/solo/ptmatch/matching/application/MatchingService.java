package com.solo.ptmatch.matching.application;

import com.solo.ptmatch.common.exception.ErrorCode;
import com.solo.ptmatch.common.exception.GlobalException;
import com.solo.ptmatch.matching.domain.Matching;
import com.solo.ptmatch.matching.domain.MatchingSchedule;
import com.solo.ptmatch.matching.domain.MatchingStatus;
import com.solo.ptmatch.matching.domain.MatchingUserInfo;
import com.solo.ptmatch.matching.domain.SessionStatus;
import com.solo.ptmatch.matching.infrastructure.AvailableScheduleRepository;
import com.solo.ptmatch.matching.infrastructure.MatchingRepository;
import com.solo.ptmatch.matching.infrastructure.MatchingScheduleRepository;
import com.solo.ptmatch.matching.presentation.request.MatchingRequestCreateRequest;
import com.solo.ptmatch.matching.presentation.request.MatchingRespondRequest;
import com.solo.ptmatch.matching.presentation.response.MatchingDetailResponse;
import com.solo.ptmatch.matching.presentation.response.MatchingReceivedSummaryResponse;
import com.solo.ptmatch.matching.presentation.response.MatchingRequestCreateResponse;
import com.solo.ptmatch.matching.presentation.response.MatchingScheduleDetailResponse;
import com.solo.ptmatch.matching.presentation.response.MatchingSentSummaryResponse;
import com.solo.ptmatch.matching.presentation.response.ReviewInfo;
import java.time.LocalDateTime;
import java.util.List;
import com.solo.ptmatch.trainer.domain.AvailableSchedule;
import com.solo.ptmatch.trainer.domain.ReservationStatus;
import com.solo.ptmatch.trainer.domain.TrainerProfile;
import com.solo.ptmatch.trainer.infrastructure.TrainerProfileRepository;
import com.solo.ptmatch.user.domain.User;
import com.solo.ptmatch.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.solo.ptmatch.review.infrastructure.ReviewRepository;

@Slf4j
@RequiredArgsConstructor
@Service
public class MatchingService {

    private final UserRepository userRepository;
    private final AvailableScheduleRepository availableScheduleRepository;
    private final MatchingRepository matchingRepository;
    private final TrainerProfileRepository trainerProfileRepository;
    private final ReviewRepository reviewRepository;
    private final MatchingScheduleRepository matchingScheduleRepository;

    // 매칭 신청 응답 (수락/거절)
    @Transactional
    public void respondMatching(Long matchingId, String userEmail, MatchingRespondRequest request) {
        // 1. 트레이너 정보 조회
        User trainerUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> GlobalException.of(ErrorCode.USER_NOT_FOUND));

        // 2. 매칭 정보 조회 (스케줄 정보 포함)
        Matching matching = matchingRepository.findByIdWithUserAndSchedules(matchingId)
                .orElseThrow(() -> GlobalException.of(ErrorCode.MATCHING_NOT_FOUND));

        // 3. 인가(Authorization): 이 매칭이 현재 트레이너의 것인지 확인
        if (!matching.getTrainerProfile().getUser().equals(trainerUser)) {
            throw GlobalException.of(ErrorCode.FORBIDDEN);
        }

        // 4. 요청에 따라 상태 분기 처리(수락/거절) - 추후 알림 기능 추가 예정
        switch (request.status()) {
            case ACCEPTED -> {
                matching.accept();
                matching.getSchedules().forEach(schedule -> schedule.getAvailableSchedule().markAsConfirmed());
                // TODO: 수락 알림 등 후속 처리
            }
            case REJECTED -> {
                matching.reject();
                matching.getSchedules().forEach(schedule -> schedule.getAvailableSchedule().markAsAvailable());
                // TODO: 거절 알림 등 후속 처리
            }
        }
    }

    @Transactional
    public void cancelMatching(Long matchingId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> GlobalException.of(ErrorCode.USER_NOT_FOUND));

        Matching matching = matchingRepository.findById(matchingId)
                .orElseThrow(() -> GlobalException.of(ErrorCode.MATCHING_NOT_FOUND));

        if (!matching.canCancel()) {
            throw GlobalException.of(ErrorCode.MATCHING_CANNOT_BE_CANCELED);
        }

        LocalDateTime now = LocalDateTime.now();
        validateCancelDeadline(matching, now);

        
        restoreAndRemoveCancelableSchedules(matching, now);
        if (matching.getRemainingSessions() == matching.getSchedules().size()) {
            matching.cancel();
        } else {
            matching.complete();
        }
    }
    // 보낸 매칭 신청 목록 조회(매칭 스케줄은 별도 API 구성)
    @Transactional(readOnly = true)
    public Page<MatchingSentSummaryResponse> getSentMatchings(String userEmail, List<MatchingStatus> status, Pageable pageable) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> GlobalException.of(ErrorCode.USER_NOT_FOUND));

        // N+1 -> fetch join (트레이너 프로필, 유저(트레이너))
        Page<Matching> matchings = matchingRepository.findAllByUserIdWithDetails(user.getId(), status, pageable);

        return matchings.map(MatchingSentSummaryResponse::from);
    }

    // 받은 매칭 신청 목록 조회(트레이너)
    @Transactional(readOnly = true)
    public Page<MatchingReceivedSummaryResponse> getReceivedMatchings(String userEmail, List<MatchingStatus> status, Pageable pageable) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> GlobalException.of(ErrorCode.USER_NOT_FOUND));

        TrainerProfile trainerProfile = trainerProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> GlobalException.of(ErrorCode.TRAINER_PROFILE_NOT_FOUND));

        Page<Matching> matchings = matchingRepository.findAllByTrainerProfileIdAndStatus(trainerProfile.getId(), status, pageable);

        return matchings.map(MatchingReceivedSummaryResponse::from);
    }

    // 매칭 상세 조회
    @Transactional(readOnly = true)
    public MatchingDetailResponse getMatchingDetail(String userEmail, Long matchingId) {
        /*
        1. 매칭 조회
        2. 매칭id, 매칭 상태, 신청 메시지, 회원 이름, 회원 이메일, 회원 전화번호, 상품 정보(상품id, 상품명, 회당 가격, 세션 횟수), 매칭 스케줄 정보(시작 시간, 종료 시간)
         */
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> GlobalException.of(ErrorCode.USER_NOT_FOUND));

        Matching matching = matchingRepository.findByIdWithDetails(matchingId, user.getId())
                .orElseThrow(() -> GlobalException.of(ErrorCode.MATCHING_NOT_FOUND));

        ReviewInfo reviewInfo = reviewRepository
                .findByMatchingId(matchingId)
                .map(ReviewInfo::from)
                .orElse(null);

        return MatchingDetailResponse.from(matching, reviewInfo);
    }

    // 내 전체 스케줄 조회
    @Transactional(readOnly = true)
    public List<MatchingScheduleDetailResponse> getMyAllSchedules(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> GlobalException.of(ErrorCode.USER_NOT_FOUND));

        List<MatchingSchedule> schedules = matchingScheduleRepository.findAllByUserIdWithDetails(user.getId());

        return schedules.stream()
                .map(MatchingScheduleDetailResponse::from)
                .toList();
    }

    // 트레이너 대시보드용 전체 스케줄 조회
    @Transactional(readOnly = true)
    public List<MatchingScheduleDetailResponse> getTrainerDashboardSchedules(String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> GlobalException.of(ErrorCode.USER_NOT_FOUND));

        TrainerProfile trainerProfile = trainerProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> GlobalException.of(ErrorCode.TRAINER_PROFILE_NOT_FOUND));

        List<MatchingSchedule> schedules = matchingScheduleRepository
                .findAllByTrainerProfileIdWithDetails(trainerProfile.getId());

        return schedules.stream()
                .map(MatchingScheduleDetailResponse::from)
                .toList();
    }

    // 매칭 스케줄 완료 처리
    @Transactional
    public void completeSchedule(Long scheduleId, String userEmail) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> GlobalException.of(ErrorCode.USER_NOT_FOUND));

        MatchingSchedule schedule = matchingScheduleRepository.findById(scheduleId)
                .orElseThrow(() -> GlobalException.of(ErrorCode.MATCHING_SCHEDULE_NOT_FOUND));

        Matching matching = schedule.getMatching();

        if (!matching.getTrainerProfile().getUser().getId().equals(user.getId())) {
            throw GlobalException.of(ErrorCode.FORBIDDEN);
        }

        schedule.complete();

        matching.completeSession();
        if (matching.getRemainingSessions() <= 0) {
            matching.complete();
        }
    }

    private void validateCancelDeadline(Matching matching, LocalDateTime now) {
        LocalDateTime lastScheduleStartTime = matching.getSchedules().stream()
                .map(MatchingSchedule::getStartTime)
                .max(LocalDateTime::compareTo)
                .orElseThrow(() -> GlobalException.of(ErrorCode.MATCHING_SCHEDULE_NOT_FOUND));

        LocalDateTime deadline = lastScheduleStartTime.minusHours(24);
        if (now.isAfter(deadline)) {
            throw GlobalException.of(ErrorCode.MATCHING_CANCEL_DEADLINE_EXCEEDED);
        }
    }

    private void restoreAndRemoveCancelableSchedules(Matching matching, LocalDateTime now) {
        List<MatchingSchedule> cancelTargets = matching.getSchedules().stream()
                .filter(schedule -> schedule.getSessionStatus() == SessionStatus.SCHEDULED)
                .filter(schedule -> schedule.getStartTime().isAfter(now))
                .toList();

        cancelTargets.forEach(schedule -> {
            schedule.getAvailableSchedule().markAsAvailable();
            matching.removeSchedule(schedule);
        });
    }
}
