package com.solo.ptmatch.payment.application;

import com.solo.ptmatch.common.exception.ErrorCode;
import com.solo.ptmatch.common.exception.GlobalException;
import com.solo.ptmatch.matching.domain.Matching;
import com.solo.ptmatch.matching.domain.MatchingSchedule;
import com.solo.ptmatch.matching.domain.MatchingUserInfo;
import com.solo.ptmatch.matching.infrastructure.AvailableScheduleRepository;
import com.solo.ptmatch.matching.infrastructure.MatchingRepository;
import com.solo.ptmatch.payment.domain.PaymentOrder;
import com.solo.ptmatch.payment.domain.PaymentStatus;
import com.solo.ptmatch.payment.infrastructure.PaymentOrderRepository;
import com.solo.ptmatch.payment.presentation.request.PaymentConfirmRequest;
import com.solo.ptmatch.payment.presentation.response.PaymentConfirmResponse;
import com.solo.ptmatch.trainer.domain.AvailableSchedule;
import com.solo.ptmatch.trainer.domain.ReservationStatus;
import com.solo.ptmatch.user.domain.User;
import com.solo.ptmatch.user.infrastructure.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentConfirmTxService {

    private final UserRepository userRepository;
    private final PaymentOrderRepository paymentOrderRepository;
    private final AvailableScheduleRepository availableScheduleRepository;
    private final MatchingRepository matchingRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PreConfirmResult preConfirm(String userEmail, PaymentConfirmRequest request) {
        // 1. 사용자 조회
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> GlobalException.of(ErrorCode.USER_NOT_FOUND));

        // 2. 🔒 결제 정보 조회
        PaymentOrder paymentOrder = paymentOrderRepository.findByOrderIdWithLock(request.orderId())
                .orElseThrow(() -> GlobalException.of(ErrorCode.PAYMENT_ORDER_NOT_FOUND));

        // 3. 소유자 검증
        validateOwner(user, paymentOrder);

        // 4. 이미 완료된 주문은 멱등하게 동일 응답 반환
        if (paymentOrder.isDone()) {
            return PreConfirmResult.alreadyDone(PaymentConfirmResponse.from(paymentOrder));
        }

        // 5. 주문 상태/금액 검증(위변조 방지)
        validateConfirmableState(paymentOrder);
        validateRequestAmount(paymentOrder, request.amount());

        // 6. 스케줄 검증 및 예약(PENDING), 매칭 생성 후 APPROVING 전이
        List<AvailableSchedule> schedules = reserveSchedules(paymentOrder);
        Matching matching = createMatching(paymentOrder, schedules);
        paymentOrder.assignMatching(matching);
        paymentOrder.markApproving();

        return PreConfirmResult.proceed(paymentOrder.getOrderId(), paymentOrder.getAmount());
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PaymentConfirmResponse finalizeSuccess(String orderId, String paymentKey, LocalDateTime approvedAt) {
        // 7. 결제 정보 재조회 및 락
        PaymentOrder paymentOrder = paymentOrderRepository.findByOrderIdWithLock(orderId)
                .orElseThrow(() -> GlobalException.of(ErrorCode.PAYMENT_ORDER_NOT_FOUND));

        if (paymentOrder.isDone()) {
            return PaymentConfirmResponse.from(paymentOrder);
        }

        Matching matching = paymentOrder.getMatching();
        if (matching == null) {
            throw GlobalException.of(ErrorCode.INTERNAL_SERVER_ERROR);
        }

        // 8. 결제 완료 -> 매칭 및 스케줄 확정
        paymentOrder.markDone(paymentKey, approvedAt);
        matching.markPending();
        matching.getSchedules().forEach(schedule -> schedule.getAvailableSchedule().markAsConfirmed());
        return PaymentConfirmResponse.from(paymentOrder);
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void finalizeRejected(String orderId, String failedCode, String failedMessage) {
        PaymentOrder paymentOrder = paymentOrderRepository.findByOrderIdWithLock(orderId)
                .orElseThrow(() -> GlobalException.of(ErrorCode.PAYMENT_ORDER_NOT_FOUND));

        // 주문 실패 처리 및 매칭/스케줄 롤백
        paymentOrder.markFailed(failedCode, failedMessage);
        releaseMatching(paymentOrder);
    }

    private List<AvailableSchedule> reserveSchedules(PaymentOrder paymentOrder) {
        List<Long> requestedScheduleIds = paymentOrder.getRequestedScheduleIdList();

        // 스케줄 조회 및 락
        List<AvailableSchedule> schedules = availableScheduleRepository.findAllByIdInWithLock(requestedScheduleIds);

        if (schedules.size() != requestedScheduleIds.size()) {
            throw GlobalException.of(ErrorCode.AVAILABLE_SCHEDULE_NOT_FOUND);
        }

        if (schedules.stream().anyMatch(schedule -> schedule.getReservationStatus() != ReservationStatus.AVAILABLE)) {
            throw GlobalException.of(ErrorCode.SCHEDULE_ALREADY_RESERVED);
        }

        schedules.forEach(AvailableSchedule::markAsPending);
        return schedules;
    }

    private Matching createMatching(PaymentOrder paymentOrder, List<AvailableSchedule> schedules) {
        Matching matching = Matching.create(
                paymentOrder.getUser(),
                paymentOrder.getTrainerProfile(),
                paymentOrder.getRequestMessage(),
                MatchingUserInfo.of(paymentOrder.getCustomerName(), paymentOrder.getCustomerEmail(), paymentOrder.getCustomerPhone()),
                paymentOrder.getTrainerProfile().getPricePerSession());
        matching.markPaymentPending();

        for (AvailableSchedule schedule : schedules) {
            matching.addSchedule(MatchingSchedule.from(schedule));
        }

        return matchingRepository.save(matching);
    }

    private void releaseMatching(PaymentOrder paymentOrder) {
        Matching matching = paymentOrder.getMatching();
        if (matching == null) {
            return;
        }

        matching.cancel();
        matching.getSchedules().forEach(schedule -> schedule.getAvailableSchedule().markAsAvailable());
    }

    private void validateOwner(User user, PaymentOrder paymentOrder) {
        if (!paymentOrder.getUser().getId().equals(user.getId())) {
            throw GlobalException.of(ErrorCode.FORBIDDEN);
        }
    }

    private void validateConfirmableState(PaymentOrder paymentOrder) {
        if (paymentOrder.isExpired(LocalDateTime.now())) {
            throw GlobalException.of(ErrorCode.PAYMENT_ORDER_EXPIRED);
        }

        if (paymentOrder.getStatus() != PaymentStatus.READY) {
            throw GlobalException.of(ErrorCode.PAYMENT_ALREADY_PROCESSED);
        }
    }

    private void validateRequestAmount(PaymentOrder paymentOrder, Integer requestAmount) {
        if (!paymentOrder.getAmount().equals(requestAmount)) {
            throw GlobalException.of(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
    }
}
