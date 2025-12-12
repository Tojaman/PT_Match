package com.solo.ptmatch.matching.application;

import com.solo.ptmatch.common.exception.ErrorCode;
import com.solo.ptmatch.common.exception.GlobalException;
import com.solo.ptmatch.matching.domain.Matching;
import com.solo.ptmatch.matching.domain.MatchingSchedule;
import com.solo.ptmatch.matching.domain.MatchingStatus;
import com.solo.ptmatch.matching.domain.MatchingUserInfo;
import com.solo.ptmatch.matching.infrastructure.AvailableScheduleRepository;
import com.solo.ptmatch.matching.infrastructure.MatchingRepository;
import com.solo.ptmatch.matching.presentation.request.MatchingRequestCreateRequest;
import com.solo.ptmatch.matching.presentation.request.MatchingRespondRequest;
import com.solo.ptmatch.matching.presentation.response.MatchingDetailResponse;
import com.solo.ptmatch.matching.presentation.response.MatchingReceivedSummaryResponse;
import com.solo.ptmatch.matching.presentation.response.MatchingRequestCreateResponse;
import com.solo.ptmatch.matching.presentation.response.MatchingSentSummaryResponse;
import java.util.List;
import java.util.Objects;
import com.solo.ptmatch.product.infrastructure.ProductRepository;
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

@Slf4j
@RequiredArgsConstructor
@Service
public class MatchingService {

    private final UserRepository userRepository;
    private final AvailableScheduleRepository availableScheduleRepository;
    private final MatchingRepository matchingRepository;
    private final TrainerProfileRepository trainerProfileRepository;

    // PT 신청
    @Transactional
    public MatchingRequestCreateResponse requestMatching(String userEmail, MatchingRequestCreateRequest request) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> GlobalException.of(ErrorCode.USER_NOT_FOUND));
        TrainerProfile trainerProfile = trainerProfileRepository.findById(request.trainerProfileId())
                .orElseThrow(() -> GlobalException.of(ErrorCode.TRAINER_PROFILE_NOT_FOUND));

        // 1. 매칭 엔티티 생성
        MatchingUserInfo matchingUserInfo = MatchingUserInfo.from(request.userInfo());
        Matching matching = Matching.create(user, trainerProfile, request.message(), matchingUserInfo);

        // 2. 매칭 엔티티에 매칭 스케줄 추가(세션 횟수만큼)
        List<AvailableSchedule> schedules = availableScheduleRepository.findAllByIdInWithLock(request.availableScheduleIds()); // 공유 락 획득
        for (AvailableSchedule schedule : schedules) {
            if (schedule == null) {
                throw GlobalException.of(ErrorCode.AVAILABLE_SCHEDULE_NOT_FOUND);
            }
            if (schedule.getReservationStatus() != ReservationStatus.AVAILABLE) {
                throw GlobalException.of(ErrorCode.SCHEDULE_ALREADY_RESERVED);
            }
            schedule.markAsPending(); // 베타 락 승격
            matching.addSchedule(MatchingSchedule.from(schedule));
        }
        // 3. 최종 매칭 엔티티 저장
        Matching savedMatching = matchingRepository.save(matching);
        return MatchingRequestCreateResponse.from(savedMatching);
    }

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

    // 보낸 매칭 신청 목록 조회(매칭 스케줄은 별도 API 구성)
    @Transactional(readOnly = true)
    public List<MatchingSentSummaryResponse> getSentMatchings(String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> GlobalException.of(ErrorCode.USER_NOT_FOUND));

        // 신청한 매칭 목록 조회
        // N+1 -> fetch join (트레이너 프로필, 유저(트레이너))
        List<Matching> matchings = matchingRepository.findAllByUserIdWithDetails(user.getId());

        // 매칭 id, 매칭 상태, 트레이너 이름 응답
        return matchings.stream()
                .map(MatchingSentSummaryResponse::from)
                .toList();
    }

    // 받은 매칭 신청(PENDING) 목록 조회(트레이너)
    @Transactional(readOnly = true)
    public Page<MatchingReceivedSummaryResponse> getReceivedMatchings(String userEmail, List<MatchingStatus> status, Pageable pageable) {
        /* 세부 내용은 별개 API 구현
        1. 유저id(트레이너)로 매칭 리스트 조회
        2. 매칭 상태가 PENDING인 매칭 리스트 조회
        3. 매칭 id, 신청일시, 상품 제목 응답
         */

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

        Matching matching = matchingRepository.findByIdWithDetails(matchingId)
                .orElseThrow(() -> GlobalException.of(ErrorCode.MATCHING_NOT_FOUND));

        // 회원 or 트레이너의 매칭이 아닌 경우 예외처리
        if (!Objects.equals(matching.getUser().getId(), user.getId()) && !Objects.equals(matching.getTrainerProfile().getUser().getId(), user.getId())) {
            throw GlobalException.of((ErrorCode.FORBIDDEN));
        }

        return MatchingDetailResponse.from(matching);
    }
}