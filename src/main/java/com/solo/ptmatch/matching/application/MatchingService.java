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
import com.solo.ptmatch.matching.presentation.response.MatchingRespondResponse;
import com.solo.ptmatch.matching.presentation.response.MatchingScheduleSummary;
import com.solo.ptmatch.matching.presentation.response.MatchingSentSummaryResponse;
import java.util.List;

import com.solo.ptmatch.product.domain.Product;
import com.solo.ptmatch.product.infrastructure.ProductRepository;
import com.solo.ptmatch.trainer.domain.AvailableSchedule;
import com.solo.ptmatch.trainer.domain.ReservationStatus;
import com.solo.ptmatch.trainer.domain.TrainerProfile;
import com.solo.ptmatch.trainer.infrastructure.TrainerProfileRepository;
import com.solo.ptmatch.user.domain.User;
import com.solo.ptmatch.user.infrastructure.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
@Service
public class MatchingService {

    private final UserRepository userRepository;
    private final AvailableScheduleRepository availableScheduleRepository;
    private final MatchingRepository matchingRepository;
    private final ProductRepository productRepository;
    private final TrainerProfileRepository trainerProfileRepository;

    // PT 신청
    @Transactional
    public MatchingRequestCreateResponse requestMatching(String userEmail, MatchingRequestCreateRequest request) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> GlobalException.of(ErrorCode.USER_NOT_FOUND));
        TrainerProfile trainerProfile = trainerProfileRepository.findById(request.trainerProfileId())
                .orElseThrow(() -> GlobalException.of(ErrorCode.TRAINER_PROFILE_NOT_FOUND));
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> GlobalException.of(ErrorCode.PRODUCT_NOT_FOUND));

        /*
         1. findAllByIdIn(request.availableScheduleIds())으로 모든  스케줄 조회
         2. 스케줄 순환
         3. 예외 검증
         4. Pending 변경 -> 더티체크 스케줄 업데이트
         5. MatchingSchedules 객체 생성
         6. Matching.addSchedule(MatchingSchedules 객체)
         7. Matching.save() -> 매칭 저장, 매칭 스케줄 저장
         */

        MatchingUserInfo matchingUserInfo = MatchingUserInfo.of(
                request.userInfo().name(),
                request.userInfo().email(),
                request.userInfo().phoneNumber());

        Matching matching = Matching.create(user, trainerProfile, product, request.message(), matchingUserInfo);

        List<AvailableSchedule> schedules = availableScheduleRepository.findAllByIdIn(request.availableScheduleIds());
        for (AvailableSchedule schedule : schedules) {
            if (schedule == null) {
                throw GlobalException.of(ErrorCode.AVAILABLE_SCHEDULE_NOT_FOUND);
            }
            if (schedule.getReservationStatus() != ReservationStatus.AVAILABLE) {
                throw GlobalException.of(ErrorCode.SCHEDULE_ALREADY_RESERVED);
            }
            schedule.markAsPending();
            matching.addSchedule(MatchingSchedule.from(schedule));
        }

        Matching savedMatching = matchingRepository.save(matching);

        List<MatchingScheduleSummary> scheduleSummaries = savedMatching.getSchedules().stream()
                .map(schedule -> MatchingScheduleSummary.from(
                        schedule.getId(),
                        schedule.getAvailableSchedule().getId(),
                        schedule.getStartTime(),
                        schedule.getEndTime(),
                        schedule.getSessionStatus()
                ))
                .toList();

        return MatchingRequestCreateResponse.of(
                savedMatching.getId(),
                savedMatching.getMatchingStatus(),
                scheduleSummaries,
                matchingUserInfo
        );
    }

    // 보낸 매칭 신청 목록 조회(매칭 스케줄은 별도 API 구성)
    @Transactional(readOnly = true)
    public List<MatchingSentSummaryResponse> getSentMatchings(String userEmail) {

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> GlobalException.of(ErrorCode.USER_NOT_FOUND));

        // 신청한 매칭 목록 조회
        // N+1 -> fetch join (트레이너 프로필, 유저(트레이너), 상품)
        List<Matching> matchings = matchingRepository.findAllByUserIdWithDetails(user.getId());

        // 매칭 id, 매칭 상태, 상품 제목, 트레이너 이름 응답
        return matchings.stream()
                .map(MatchingSentSummaryResponse::of)
                .toList();
    }

    // 받은 매칭 신청(PENDING) 목록 조회(트레이너)
    @Transactional(readOnly = true)
    public List<MatchingReceivedSummaryResponse> getReceivedMatchings(String userEmail) {
        /* 세부 내용은 별개 API 구현
        1. 유저id(트레이너)로 매칭 리스트 조회
        2. 매칭 상태가 PENDING인 매칭 리스트 조회
        3. 매칭 id, 신청일시, 상품 제목 응답
         */

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> GlobalException.of(ErrorCode.USER_NOT_FOUND));

        TrainerProfile trainerProfile = trainerProfileRepository.findByTrainerId(user.getId())
                .orElseThrow(() -> GlobalException.of(ErrorCode.TRAINER_PROFILE_NOT_FOUND));

        List<Matching> matchings = matchingRepository.findAllByTrainerProfileIdAndStatusWithProduct(trainerProfile.getId(), MatchingStatus.PENDING);
        log.info("매칭 리스트: {}", matchings.stream().toList());
        log.info("트레이너 프로필 아이디: {}", trainerProfile.getId());

        return matchings.stream()
                .map(MatchingReceivedSummaryResponse::of)
                .toList();
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
        if (matching.getUser().getId() != user.getId() && matching.getTrainerProfile().getTrainer().getId() != user.getId()) {
            throw GlobalException.of((ErrorCode.FORBIDDEN));
        }

        return MatchingDetailResponse.of(matching);
    }

    // 매칭 신청 응답 (수락/거절)
    @Transactional
    public void respondMatching(Long matchingId, String userEmail, MatchingRespondRequest request) {
        // 1. 트레이너 정보 조회
        User trainerUser = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> GlobalException.of(ErrorCode.USER_NOT_FOUND));

        // 2. 매칭 정보 조회 (스케줄 정보 포함)
        Matching matching = matchingRepository.findByIdWithTrainerAndSchedules(matchingId)
                .orElseThrow(() -> GlobalException.of(ErrorCode.MATCHING_NOT_FOUND));

        // 3. 인가(Authorization): 이 매칭이 현재 트레이너의 것인지 확인
        if (!matching.getTrainerProfile().getTrainer().equals(trainerUser)) {
            throw GlobalException.of(ErrorCode.FORBIDDEN);
        }

        // 4. 요청에 따라 상태 분기 처리
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
}
