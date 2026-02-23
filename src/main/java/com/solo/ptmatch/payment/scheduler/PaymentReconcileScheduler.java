package com.solo.ptmatch.payment.scheduler;

import com.solo.ptmatch.payment.application.PaymentConfirmTxService;
import com.solo.ptmatch.payment.application.PaymentRetryPolicy;
import com.solo.ptmatch.payment.domain.PaymentOrder;
import com.solo.ptmatch.payment.domain.PaymentStatus;
import com.solo.ptmatch.payment.infrastructure.PaymentOrderRepository;
import com.solo.ptmatch.payment.infrastructure.toss.TossPaymentsClient;
import com.solo.ptmatch.payment.infrastructure.toss.TossPaymentsException;
import com.solo.ptmatch.payment.infrastructure.toss.TossServerException;
import com.solo.ptmatch.payment.infrastructure.toss.dto.TossPaymentResponse;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentReconcileScheduler {

    private final PaymentOrderRepository paymentOrderRepository;
    private final PaymentConfirmTxService paymentConfirmTxService;
    private final TossPaymentsClient tossPaymentsClient;
    private final PaymentRetryPolicy paymentRetryPolicy;

    // UNKNOWN 상태 & 재시도 시점 도래 -> 재조회
    @Scheduled(fixedDelayString = "${payment.retry.reconcile-interval}")
    public void reconcileUnknownOrders() {
        LocalDateTime now = LocalDateTime.now();
        List<PaymentOrder> orders = paymentOrderRepository.findRetryTargetOrders(
                PaymentStatus.UNKNOWN,
                now,
                PageRequest.of(0, paymentRetryPolicy.reconcileBatchSize()));

        for (PaymentOrder order : orders) {
            reconcileOrder(order, now);
        }
    }

    // APPROVING 상태가 장기 체류 -> 재조회
    @Scheduled(fixedDelayString = "${payment.retry.reconcile-interval}")
    public void reconcileStaleApprovingOrders() {
        LocalDateTime now = LocalDateTime.now();
        List<PaymentOrder> orders = paymentOrderRepository.findStaleOrders(
                PaymentStatus.APPROVING,
                paymentRetryPolicy.approvingStaleBefore(now),
                PageRequest.of(0, paymentRetryPolicy.reconcileBatchSize()));

        for (PaymentOrder order : orders) {
            reconcileOrder(order, now);
        }
    }

    // 단일 재조회(단일 트랜잭션)
    private void reconcileOrder(PaymentOrder paymentOrder, LocalDateTime now) {
        String orderId = paymentOrder.getOrderId();
        try {
            TossPaymentResponse response = tossPaymentsClient.getPaymentByOrderId(orderId);

            // DONE 상태 -> 결제/매칭/스케줄 최종 성공
            if (isDone(response.status())) {
                paymentConfirmTxService.finalizeSuccess(orderId, response.paymentKey(),
                        response.approvedLocalDateTime());
                return;
            }

            // 취소/실패 상태 -> 주문 실패 확정 및 롤백
            if (isFailed(response.status())) {
                paymentConfirmTxService.finalizeRejected(orderId, response.status(), "주문 실패");
                return;
            }

            // IN_PROGRESS -> 결제 승인 요청이 미전달된 것으로 판단, POST /confirm 재시도
            if (isInProgress(response.status())) {
                retryConfirm(paymentOrder, now);
                return;
            }

            // 토스 API status 추가를 대비한 방어 코드(현재 실행하지 않음)
            log.error("[DEFENSE] 처리 불가한 Toss status 감지. orderId={}, status={}", orderId, response.status());
            paymentConfirmTxService.finalizeRejected(orderId, "RECONCILE_UNEXPECTED_STATUS",
                    "처리 불가한 Toss status가 감지되었습니다: " + response.status());
        } catch (TossServerException exception) { // 응답 없음 -> 재조회 갱신
            paymentConfirmTxService.handleReconcileRetryFailure(orderId, exception.getProviderCode(),
                    exception.getProviderMessage(), now);
        } catch (TossPaymentsException exception) { // 결제 실패 -> 주문 실패 확정 및 롤백
            paymentConfirmTxService.finalizeRejected(orderId, exception.getProviderCode(),
                    exception.getProviderMessage());
        } catch (Exception exception) { // 재조회 자체 실패 -> 재조회 갱신
            log.error("결제 재조회 처리 중 예외 발생. orderId={}", orderId, exception);
            paymentConfirmTxService.handleReconcileRetryFailure(orderId, "RECONCILE_INTERNAL_ERROR",
                    exception.getMessage(), now);
        }
    }

    // IN_PROGRESS -> POST /confirm 재시도
    private void retryConfirm(PaymentOrder paymentOrder, LocalDateTime now) {
        String orderId = paymentOrder.getOrderId();
        try {
            TossPaymentResponse confirmResponse = tossPaymentsClient.confirm(
                    paymentOrder.getPaymentKey(), orderId, paymentOrder.getAmount());
            paymentConfirmTxService.finalizeSuccess(orderId, confirmResponse.paymentKey(),
                    confirmResponse.approvedLocalDateTime());
        } catch (TossServerException exception) { // 5xx -> 재시도 대기
            paymentConfirmTxService.handleReconcileRetryFailure(orderId, exception.getProviderCode(),
                    exception.getProviderMessage(), now);
        } catch (TossPaymentsException exception) { // 4xx -> 실패 확정
            paymentConfirmTxService.finalizeRejected(orderId, exception.getProviderCode(),
                    exception.getProviderMessage());
        } catch (Exception exception) { // 내부 오류 -> 재시도 대기
            log.error("결제 재승인 처리 중 예외 발생. orderId={}", orderId, exception);
            paymentConfirmTxService.handleReconcileRetryFailure(orderId, "RECONCILE_CONFIRM_ERROR",
                    exception.getMessage(), now);
        }
    }

    // Toss 상태가 최종 승인(DONE)인지 판별
    private boolean isDone(String status) {
        return "DONE".equalsIgnoreCase(status);
    }

    // Toss 상태가 최종 실패(ABORTED/EXPIRED/CANCELED)인지 판별
    private boolean isFailed(String status) {
        String normalized = status.toUpperCase(Locale.ROOT);
        return normalized.equals("ABORTED")
                || normalized.equals("EXPIRED")
                || normalized.equals("CANCELED");
    }

    // Toss 상태가 승인 대기(IN_PROGRESS)인지 판별 -> POST /confirm 재시도 대상
    private boolean isInProgress(String status) {
        return "IN_PROGRESS".equalsIgnoreCase(status);
    }
}
