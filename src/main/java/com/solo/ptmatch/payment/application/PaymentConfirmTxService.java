package com.solo.ptmatch.payment.application;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.solo.ptmatch.common.exception.ErrorCode;
import com.solo.ptmatch.common.exception.GlobalException;
import com.solo.ptmatch.matching.domain.Matching;
import com.solo.ptmatch.matching.domain.MatchingSchedule;
import com.solo.ptmatch.matching.domain.MatchingUserInfo;
import com.solo.ptmatch.matching.infrastructure.AvailableScheduleRepository;
import com.solo.ptmatch.matching.infrastructure.MatchingRepository;
import com.solo.ptmatch.payment.domain.PaymentCompensationJob;
import com.solo.ptmatch.payment.domain.PaymentOrder;
import com.solo.ptmatch.payment.domain.PaymentStatus;
import com.solo.ptmatch.payment.infrastructure.PaymentCompensationJobRepository;
import com.solo.ptmatch.payment.infrastructure.PaymentOrderRepository;
import com.solo.ptmatch.payment.presentation.request.PaymentConfirmRequest;
import com.solo.ptmatch.payment.presentation.response.PaymentConfirmResponse;
import com.solo.ptmatch.payment.presentation.response.PaymentOrderStatusResponse;
import com.solo.ptmatch.trainer.domain.AvailableSchedule;
import com.solo.ptmatch.trainer.domain.ReservationStatus;
import com.solo.ptmatch.user.domain.User;
import com.solo.ptmatch.user.infrastructure.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class PaymentConfirmTxService {

    private final UserRepository userRepository;
    private final PaymentOrderRepository paymentOrderRepository;
    private final PaymentCompensationJobRepository paymentCompensationJobRepository;
    private final AvailableScheduleRepository availableScheduleRepository;
    private final MatchingRepository matchingRepository;
    private final PaymentRetryPolicy paymentRetryPolicy;

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

    // =============== 토스 결과 ===============
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PaymentConfirmResponse finalizeSuccess(String orderId, String paymentKey, LocalDateTime approvedAt) {
        PaymentOrder paymentOrder = paymentOrderRepository.findByOrderIdWithLock(orderId)
                .orElseThrow(() -> GlobalException.of(ErrorCode.PAYMENT_ORDER_NOT_FOUND));

        if (paymentOrder.isDone()) {
            return PaymentConfirmResponse.from(paymentOrder);
        }

        Matching matching = paymentOrder.getMatching();
        paymentOrder.markDone(paymentKey, approvedAt);
        matching.markPending();
        matching.getSchedules().forEach(schedule -> schedule.getAvailableSchedule().markAsConfirmed());
        return PaymentConfirmResponse.from(paymentOrder);
    }

    // 결제 실패 -> 주문 실패 확정 및 롤백
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void finalizeRejected(String orderId, String failedCode, String failedMessage) {
        PaymentOrder paymentOrder = paymentOrderRepository.findByOrderIdWithLock(orderId)
                .orElseThrow(() -> GlobalException.of(ErrorCode.PAYMENT_ORDER_NOT_FOUND));

        if (paymentOrder.isTerminal()) {
            return;
        }

        // 주문 실패 처리 및 매칭/스케줄 롤백
        paymentOrder.markFailed(failedCode, failedMessage);
        releaseMatching(paymentOrder);
    }

    // 5xx -> 주문 UNKNOWN 전이, 재시도
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PaymentConfirmResponse markUnknown(String orderId, String failedCode, String failedMessage, LocalDateTime now) {
        PaymentOrder paymentOrder = paymentOrderRepository.findByOrderIdWithLock(orderId)
                .orElseThrow(() -> GlobalException.of(ErrorCode.PAYMENT_ORDER_NOT_FOUND));

        if (paymentOrder.isTerminal()) {
            return PaymentConfirmResponse.from(paymentOrder);
        }

        paymentOrder.markUnknown(
                failedCode,
                failedMessage,
                paymentRetryPolicy.firstRetryAt(now),
                paymentRetryPolicy.resolveDeadlineAt(now));
        return PaymentConfirmResponse.from(paymentOrder);
    }

    // 결제 승인, T2 실패 -> 취소 보상 작업 등록
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public PaymentConfirmResponse markCancelPending(String orderId, String paymentKey, String failedCode, String failedMessage, String reason, String triggerSource, LocalDateTime now) {
        PaymentOrder paymentOrder = paymentOrderRepository.findByOrderIdWithLock(orderId)
                .orElseThrow(() -> GlobalException.of(ErrorCode.PAYMENT_ORDER_NOT_FOUND));

        // 스케줄러가 처리한 경우 리턴
        if (paymentOrder.isTerminal()) {
            return PaymentConfirmResponse.from(paymentOrder);
        }

        if (paymentOrder.getStatus() != PaymentStatus.CANCEL_PENDING) {
            paymentOrder.markCancelPending(
                    paymentKey,
                    failedCode,
                    failedMessage,
                    now,
                    paymentRetryPolicy.resolveDeadlineAt(now));
        }

        ensureCompensationJob(paymentOrder, reason, triggerSource, now);
        return PaymentConfirmResponse.from(paymentOrder);
    }

    // =============== 재조회 =============== 
    // 재조회 성공(DONE) -> 결제/매칭/스케줄 최종 성공
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void finalizeFromReconcileSuccess(String orderId, String paymentKey, LocalDateTime approvedAt,
            Integer totalAmount) {
        PaymentOrder paymentOrder = paymentOrderRepository.findByOrderIdWithLock(orderId)
                .orElseThrow(() -> GlobalException.of(ErrorCode.PAYMENT_ORDER_NOT_FOUND));

        if (paymentOrder.getStatus() != PaymentStatus.UNKNOWN
                && paymentOrder.getStatus() != PaymentStatus.APPROVING) {
            return;
        }

        // 재조회 응답 필수 필드 검증 (NPE 방어)
        if (approvedAt == null || totalAmount == null) {
            paymentOrder.markManualReview("RECONCILE_MISSING_FIELD", "재조회 응답에 필수 필드가 누락되었습니다.");
            return;
        }

        // 금액 위변조 검증
        if (!paymentOrder.getAmount().equals(totalAmount)) {
            paymentOrder.markManualReview("RECONCILE_AMOUNT_MISMATCH", "재조회 금액이 주문 금액과 일치하지 않습니다.");
            return;
        }

        paymentOrder.markDone(paymentKey, approvedAt);
        Matching matching = paymentOrder.getMatching();
        matching.markPending();
        matching.getSchedules().forEach(schedule -> schedule.getAvailableSchedule().markAsConfirmed());
    }

    // 재조회 실패(FAILED) -> 주문 실패 확정 및 롤백
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void finalizeFromReconcileFailure(String orderId, String tossStatus) {
        finalizeRejected(orderId, "TOSS_" + tossStatus.toUpperCase(Locale.ROOT), "재조회 결과 결제가 실패 상태로 확인되었습니다.");
    }

    // 재조회 갱신
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleReconcileRetryFailure(String orderId, String failedCode, String failedMessage, LocalDateTime now) {
        PaymentOrder paymentOrder = paymentOrderRepository.findByOrderIdWithLock(orderId)
                .orElseThrow(() -> GlobalException.of(ErrorCode.PAYMENT_ORDER_NOT_FOUND));

        // UNKNOWN으로 전이 후 재시도 메타 기록, 한계 초과 시 MANUAL_REVIEW로 격리
        if (paymentOrder.getStatus() == PaymentStatus.APPROVING) {
            paymentOrder.markUnknown(
                    failedCode,
                    failedMessage,
                    paymentRetryPolicy.firstRetryAt(now),
                    paymentRetryPolicy.resolveDeadlineAt(now));
            return;
        }

        if (paymentOrder.getStatus() != PaymentStatus.UNKNOWN) {
            return;
        }

        int nextAttemptCount = paymentOrder.getAttemptCount() + 1;
        // 재조회 만료된 경우 -> ManualReview(수동 처리)
        if (paymentRetryPolicy.isManualReview(nextAttemptCount, paymentOrder.getResolveDeadlineAt(), now)) {
            paymentOrder.markManualReview(failedCode, failedMessage);
            return;
        }

        // 재시도 대기
        paymentOrder.markRetryWaiting(
                nextAttemptCount,
                failedCode,
                failedMessage,
                paymentRetryPolicy.nextRetryAt(nextAttemptCount, now));
    }

    // =============== 보상 취소 ===============
    // 보상 스케줄러가 실행할 취소 대상(paymentKey)을 반환
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public CompensationExecutionTarget getCompensationExecutionTarget(Long compensationJobId) {
        PaymentCompensationJob compensationJob = paymentCompensationJobRepository.findByIdWithLock(compensationJobId)
                .orElseThrow(() -> GlobalException.of(ErrorCode.NOT_FOUND));
        PaymentOrder paymentOrder = compensationJob.getPaymentOrder();

        if (compensationJob.isTerminal()) {
            return null;
        }

        if (paymentOrder.getStatus() == PaymentStatus.CANCELED) {
            compensationJob.markSucceeded();
            return null;
        }

        if (paymentOrder.getStatus() != PaymentStatus.CANCEL_PENDING) {
            compensationJob.markFailedPermanent("INVALID_ORDER_STATE", "보상 대상 주문 상태가 CANCEL_PENDING이 아닙니다.");
            return null;
        }

        return new CompensationExecutionTarget(
                compensationJob.getId(),
                paymentOrder.getOrderId(),
                paymentOrder.getPaymentKey());
    }

    // 취소 보상 성공 -> 주문 CANCELED 확정
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void finalizeCompensationSuccess(Long compensationJobId) {
        PaymentCompensationJob compensationJob = paymentCompensationJobRepository.findByIdWithLock(compensationJobId)
                .orElseThrow(() -> GlobalException.of(ErrorCode.NOT_FOUND));
        PaymentOrder paymentOrder = compensationJob.getPaymentOrder();

        if (compensationJob.isTerminal()) {
            return;
        }

        if (paymentOrder.getStatus() == PaymentStatus.CANCELED) {
            compensationJob.markSucceeded();
            return;
        }

        if (paymentOrder.getStatus() != PaymentStatus.CANCEL_PENDING) {
            compensationJob.markFailedPermanent("INVALID_ORDER_STATE", "보상 대상 주문 상태가 CANCEL_PENDING이 아닙니다.");
            return;
        }

        paymentOrder.markCanceled();
        releaseMatching(paymentOrder);
        compensationJob.markSucceeded();
    }

    // 취소 보상 실패 -> 재시도 대기 또는 MANUAL_REVIEW로 격리
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handleCompensationRetryFailure(
            Long compensationJobId,
            String failedCode,
            String failedMessage,
            boolean retryable,
            LocalDateTime now) {
        PaymentCompensationJob compensationJob = paymentCompensationJobRepository.findByIdWithLock(compensationJobId)
                .orElseThrow(() -> GlobalException.of(ErrorCode.NOT_FOUND));
        PaymentOrder paymentOrder = compensationJob.getPaymentOrder();

        if (compensationJob.isTerminal()) {
            return;
        }

        if (paymentOrder.getStatus() != PaymentStatus.CANCEL_PENDING) {
            if (paymentOrder.getStatus() == PaymentStatus.CANCELED) {
                compensationJob.markSucceeded();
            } else {
                compensationJob.markFailedPermanent("INVALID_ORDER_STATE", "보상 대상 주문 상태가 CANCEL_PENDING이 아닙니다.");
            }
            return;
        }

        int nextAttemptCount = compensationJob.getAttemptCount() + 1;
        if (!retryable || paymentRetryPolicy.isManualReview(nextAttemptCount, compensationJob.getResolveDeadlineAt(), now)) {
            paymentOrder.markManualReview(failedCode, failedMessage);
            compensationJob.markFailedPermanent(failedCode, failedMessage);
            return;
        }

        LocalDateTime nextRetryAt = paymentRetryPolicy.nextRetryAt(nextAttemptCount, now);
        paymentOrder.markCancelRetryWaiting(nextAttemptCount, failedCode, failedMessage, nextRetryAt);
        compensationJob.markRetryWaiting(nextAttemptCount, failedCode, failedMessage, nextRetryAt);
    }

    // =============== TTL 만료 ===============
    // READY 상태 TTL 만료 주문 처리
    @Transactional
    public void expireReadyOrder(LocalDateTime now) {
        paymentOrderRepository.bulkExpireReadyOrders(now);
    }

    // =============== 클라이언트 폴링 ===============
    @Transactional(readOnly = true)
    public PaymentOrderStatusResponse getOrderStatus(String userEmail, String orderId) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> GlobalException.of(ErrorCode.USER_NOT_FOUND));

        PaymentOrder paymentOrder = paymentOrderRepository.findByOrderIdWithUserAndMatching(orderId)
                .orElseThrow(() -> GlobalException.of(ErrorCode.PAYMENT_ORDER_NOT_FOUND));

        validateOwner(user, paymentOrder);
        return PaymentOrderStatusResponse.from(paymentOrder);
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

    private void releaseMatching(PaymentOrder paymentOrder) {
        Matching matching = paymentOrder.getMatching();
        if (matching == null) {
            return;
        }

        matching.cancel();
        matching.getSchedules().forEach(schedule -> schedule.getAvailableSchedule().markAsAvailable());
    }

    private void ensureCompensationJob(PaymentOrder paymentOrder, String reason, String triggerSource, LocalDateTime now) {
        if (paymentCompensationJobRepository.findByPaymentOrder_Id(paymentOrder.getId()).isPresent()) {
            return;
        }

        paymentCompensationJobRepository.save(PaymentCompensationJob.pending(paymentOrder, reason, triggerSource, now, paymentRetryPolicy.resolveDeadlineAt(now)));
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
