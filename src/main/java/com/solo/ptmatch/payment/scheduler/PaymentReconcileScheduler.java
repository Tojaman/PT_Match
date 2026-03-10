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

    // 단일 재조회 (단일 트랜잭션)
    private void reconcileOrder(PaymentOrder paymentOrder, LocalDateTime now) {
        String orderId = paymentOrder.getOrderId();
        try {
            TossPaymentResponse response = tossPaymentsClient.getPaymentByOrderId(orderId);
            String status = response.status();

            // DONE -> 결제 최종 성공
            if ("DONE".equalsIgnoreCase(status)) {
                paymentConfirmTxService.finalizeSuccess(orderId, response.paymentKey(),
                        response.approvedLocalDateTime());
                return;
            }

            // EXPIRED / CANCELED / ABORTED -> 상태별 최종 처리
            if ("EXPIRED".equalsIgnoreCase(status)) {
                paymentConfirmTxService.finalizeExpired(orderId, status, "주문 만료");
                return;
            }
            if ("CANCELED".equalsIgnoreCase(status)) {
                paymentConfirmTxService.finalizeCanceled(orderId, status, "주문 취소");
                return;
            }
            if ("ABORTED".equalsIgnoreCase(status)) {
                paymentConfirmTxService.finalizeRejected(orderId, status, "주문 실패");
                return;
            }

            // IN_PROGRESS -> 결제 승인 요청이 미전달된 것으로 판단, POST /confirm 재시도
            if ("IN_PROGRESS".equalsIgnoreCase(status)) {
                retryConfirm(paymentOrder, now);
                return;
            }

            // 그 외 예상 불가 상태 -> 재시도 횟수 보고 MANUAL_REVIEW 전환 여부 결정
            log.error("[Reconcile] 처리 불가한 Toss status 감지. orderId={}, status={}", orderId, status);
            paymentConfirmTxService.handleReconcileRetryFailure(orderId, "RECONCILE_UNEXPECTED_STATUS",
                    "처리 불가한 Toss status가 감지되었습니다: " + status, now);

        } catch (TossServerException exception) { // 5xx -> 재시도 대기 (서브클래스이므로 먼저 catch)
            paymentConfirmTxService.handleReconcileRetryFailure(orderId, exception.getProviderCode(),
                    exception.getProviderMessage(), now);
        } catch (TossPaymentsException exception) { // 4xx -> 명시적 실패 확정 및 롤백
            paymentConfirmTxService.finalizeRejected(orderId, exception.getProviderCode(),
                    exception.getProviderMessage());
        } catch (Exception exception) { // 내부 오류 -> 재시도 대기
            log.error("[Reconcile] 재조회 처리 중 예외 발생. orderId={}", orderId, exception);
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
            log.error("[Reconcile] 결제 재승인 처리 중 예외 발생. orderId={}", orderId, exception);
            paymentConfirmTxService.handleReconcileRetryFailure(orderId, "RECONCILE_CONFIRM_ERROR",
                    exception.getMessage(), now);
        }
    }
}
