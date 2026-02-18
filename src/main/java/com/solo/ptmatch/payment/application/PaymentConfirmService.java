package com.solo.ptmatch.payment.application;

import com.solo.ptmatch.common.exception.ErrorCode;
import com.solo.ptmatch.common.exception.GlobalException;
import com.solo.ptmatch.matching.domain.Matching;
import com.solo.ptmatch.matching.domain.MatchingSchedule;
import com.solo.ptmatch.matching.domain.MatchingUserInfo;
import com.solo.ptmatch.matching.infrastructure.AvailableScheduleRepository;
import com.solo.ptmatch.matching.infrastructure.MatchingRepository;
import com.solo.ptmatch.payment.domain.PaymentOrder;
import com.solo.ptmatch.payment.infrastructure.PaymentOrderRepository;
import com.solo.ptmatch.payment.infrastructure.toss.TossPaymentsClient;
import com.solo.ptmatch.payment.infrastructure.toss.TossPaymentsException;
import com.solo.ptmatch.payment.infrastructure.toss.dto.TossConfirmResponse;
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
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class PaymentConfirmService {

    private final UserRepository userRepository;
    private final PaymentOrderRepository paymentOrderRepository;
    private final AvailableScheduleRepository availableScheduleRepository;
    private final MatchingRepository matchingRepository;
    private final TossPaymentsClient tossPaymentsClient;

    @Transactional
    public PaymentConfirmResponse confirm(String userEmail, PaymentConfirmRequest request) {
        User user = userRepository.findByEmail(userEmail)
                .orElseThrow(() -> GlobalException.of(ErrorCode.USER_NOT_FOUND));

        // 1. 결제 정보 조회 및 락
        PaymentOrder paymentOrder = paymentOrderRepository.findByOrderIdWithLock(request.orderId())
                .orElseThrow(() -> GlobalException.of(ErrorCode.PAYMENT_ORDER_NOT_FOUND));

        // 2. 소유자 검증
        validateOwner(user, paymentOrder);

        // 3. 이미 완료된 주문은 멱등하게 동일 응답 반환
        if (paymentOrder.isDone()) {
            return PaymentConfirmResponse.from(paymentOrder);
        }

        // 4. 주문 상태 및 금액 검증(위변조 방지)
        validateConfirmableState(paymentOrder);
        validateRequestAmount(paymentOrder, request.amount());

        // 5. 🔒 스케줄 검증 및 예약(PENDING) 
        List<AvailableSchedule> schedules = reserveSchedules(paymentOrder);
        Matching matching = createMatching(paymentOrder, schedules);
        paymentOrder.assignMatching(matching);

        // 6. 토스 결제 승인 요청
        TossConfirmResponse confirmResponse;
        try {
            confirmResponse = tossPaymentsClient.confirm(request.paymentKey(), request.orderId(), request.amount());
        } catch (TossPaymentsException exception) {
            // 토스 연동 실패 시 주문 실패 처리 및 스케줄 롤백
            rejectAndRelease(paymentOrder, exception.getProviderCode(), exception.getProviderMessage());
            if (exception.isClientError()) {
                throw GlobalException.of(ErrorCode.INVALID_REQUEST);
            }
            throw GlobalException.of(ErrorCode.PAYMENT_PROVIDER_ERROR);
        }

        // 7. 승인 응답 검증
        validateConfirmAmount(paymentOrder, confirmResponse);
        validateApprovedAt(paymentOrder, confirmResponse);

        // 8. 결제 완료 -> 매칭 및 스케줄 확정
        paymentOrder.markDone(confirmResponse.paymentKey(), confirmResponse.approvedAt().toLocalDateTime());
        matching.markPending();
        matching.getSchedules().forEach(schedule -> schedule.getAvailableSchedule().markAsConfirmed());

        return PaymentConfirmResponse.from(paymentOrder);
    }

    private List<AvailableSchedule> reserveSchedules(PaymentOrder paymentOrder) {
        List<Long> requestedScheduleIds = paymentOrder.getRequestedScheduleIdList();

        // 스케줄 조회 및 락
        List<AvailableSchedule> schedules = availableScheduleRepository.findAllByIdInWithLock(requestedScheduleIds);

        if (schedules.stream().anyMatch(schedule -> schedule.getReservationStatus() != ReservationStatus.AVAILABLE)) {
            paymentOrder.markFailed("SCHEDULE_ALREADY_RESERVED", "이미 예약 진행 중인 스케줄이 포함되어 있습니다.");
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

    // 주문 실패 처리 및 매칭/스케줄 롤백
    private void rejectAndRelease(PaymentOrder paymentOrder, String code, String message) {
        paymentOrder.markFailed(code, message);

        Matching matching = paymentOrder.getMatching();
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
            paymentOrder.markExpired();
            throw GlobalException.of(ErrorCode.PAYMENT_ORDER_EXPIRED);
        }

        if (paymentOrder.isTerminal()) {
            throw GlobalException.of(ErrorCode.PAYMENT_ALREADY_PROCESSED);
        }
    }

    private void validateRequestAmount(PaymentOrder paymentOrder, Integer requestAmount) {
        if (!paymentOrder.getAmount().equals(requestAmount)) {
            paymentOrder.markFailed("PAYMENT_AMOUNT_MISMATCH", "요청 금액이 주문 금액과 일치하지 않습니다.");
            throw GlobalException.of(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
    }

    private void validateConfirmAmount(PaymentOrder paymentOrder, TossConfirmResponse confirmResponse) {
        if (!paymentOrder.getAmount().equals(confirmResponse.totalAmount())) {
            rejectAndRelease(paymentOrder, "PAYMENT_AMOUNT_MISMATCH", "승인 응답 금액이 주문 금액과 일치하지 않습니다.");
            throw GlobalException.of(ErrorCode.PAYMENT_AMOUNT_MISMATCH);
        }
    }

    private void validateApprovedAt(PaymentOrder paymentOrder, TossConfirmResponse confirmResponse) {
        if (confirmResponse.approvedAt() == null) {
            rejectAndRelease(paymentOrder, "INVALID_PROVIDER_RESPONSE", "승인 시각이 없습니다.");
            throw GlobalException.of(ErrorCode.PAYMENT_PROVIDER_ERROR);
        }
    }
}
