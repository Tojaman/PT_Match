package com.solo.ptmatch.matching.application;

import com.solo.ptmatch.common.exception.ErrorCode;
import com.solo.ptmatch.common.exception.GlobalException;
import com.solo.ptmatch.matching.domain.Matching;
import com.solo.ptmatch.matching.domain.MatchingUserInfo;
import com.solo.ptmatch.matching.infrastructure.AvailableScheduleRepository;
import com.solo.ptmatch.matching.infrastructure.MatchingRepository;
import com.solo.ptmatch.matching.presentation.request.MatchingRequestCreateRequest;
import com.solo.ptmatch.matching.presentation.request.MatchingRespondRequest;
import com.solo.ptmatch.matching.presentation.response.MatchingDetailResponse;
import com.solo.ptmatch.matching.presentation.response.MatchingReceivedSummaryResponse;
import com.solo.ptmatch.matching.presentation.response.MatchingRequestCreateResponse;
import com.solo.ptmatch.matching.presentation.response.MatchingRespondResponse;
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
import org.springframework.security.core.parameters.P;
import org.springframework.stereotype.Service;

@RequiredArgsConstructor
@Service
public class MatchingService {

    private final UserRepository userRepository;
    private final AvailableScheduleRepository availableScheduleRepository;
    private final MatchingRepository matchingRepository;
    private final ProductRepository productRepository;
    private final TrainerProfileRepository trainerProfileRepository;

    // PT 신청
    public MatchingRequestCreateResponse requestMatching(String userEmail, MatchingRequestCreateRequest request) {
        /*
        1. 상품id, 트레이너id, 스케줄 시간(시작, 종료), 신청 메시지, 사용자 정보 담아서 요청
        2. 스케줄 존재 확인 및 상태 확인(AVAILABLE 상태인지 확인)
            2-1. 스케줄 상태가 PENDING, CONFIRMED이면 예외 발생
        3. 매칭 엔티티 생성(매칭 상태: PENDING) -> 저장
        4. 상품 정보, 트레이너 정보, 스케줄 시간, 사용자 정보 담아서 반환
         */

        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> GlobalException.of(ErrorCode.USER_NOT_FOUND));
        TrainerProfile trainerProfile = trainerProfileRepository.findById(request.trainerProfileId())
                .orElseThrow(() -> GlobalException.of(ErrorCode.TRAINER_PROFILE_NOT_FOUND));
        Product product = productRepository.findById(request.productId())
                .orElseThrow(() -> GlobalException.of(ErrorCode.PRODUCT_NOT_FOUND));

        AvailableSchedule availableSchedule = availableScheduleRepository.findByStartTimeAndEndTime(request.startTime(), request.endTile());
        if (availableSchedule == null) {
            throw GlobalException.of(ErrorCode.AVAILABLE_SCHEDULE_NOT_FOUND);
        }
        if (availableSchedule.getReservationStatus() != ReservationStatus.AVAILABLE) {
            throw GlobalException.of(ErrorCode.SCHEDULE_ALREADY_RESERVED);
        }

        MatchingUserInfo matchingUserInfo = MatchingUserInfo.of(
                request.userInfo().name(),
                request.userInfo().email(),
                request.userInfo().phoneNumber());

        Matching matching = Matching.create(user, trainerProfile, product, request.message(), matchingUserInfo);
        matchingRepository.save(matching);

        return MatchingRequestCreateResponse.of(
                matching.getId(),
                matching.getStatus(),
                availableSchedule.getStartTime(),
                availableSchedule.getEndTime(),
                matchingUserInfo);
    }

    // 보낸 매칭 신청 목록 조회
    public List<MatchingSentSummaryResponse> getSentMatchings() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    // 받은 매칭 신청 목록 조회
    public List<MatchingReceivedSummaryResponse> getReceivedMatchings() {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    // 매칭 신청 응답 (수락/거절)
    public MatchingRespondResponse respondMatching(Long matchingId, MatchingRespondRequest request) {
        throw new UnsupportedOperationException("Not implemented yet");
    }

    // 매칭 상세 조회
    public MatchingDetailResponse getMatching(Long matchingId) {
        throw new UnsupportedOperationException("Not implemented yet");
    }
}
