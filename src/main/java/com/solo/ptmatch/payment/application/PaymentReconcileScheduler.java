package com.solo.ptmatch.payment.application;

import com.solo.ptmatch.payment.domain.PaymentStatus;
import com.solo.ptmatch.payment.infrastructure.PaymentOrderRepository;
import com.solo.ptmatch.payment.infrastructure.toss.TossPaymentsClient;
import com.solo.ptmatch.payment.infrastructure.toss.TossPaymentsException;
import com.solo.ptmatch.payment.infrastructure.toss.TossServerException;
import com.solo.ptmatch.payment.infrastructure.toss.dto.TossConfirmResponse;
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

    // UNKNOWN 상태이면서 재시도 시각이 도래한 주문을 주기적으로 재조회
    @Scheduled(fixedDelayString = "${payment.retry.reconcile-interval}")
    public void reconcileUnknownOrders() {
        LocalDateTime now = LocalDateTime.now();
        List<String> orderIds = paymentOrderRepository.findRetryTargetOrderIds(
                PaymentStatus.UNKNOWN,
                now,
                PageRequest.of(0, paymentRetryPolicy.reconcileBatchSize()));

        for (String orderId : orderIds) {
            reconcileOrder(orderId, now);
        }
    }

    // 단일 주문의 Toss 상태를 조회해 DONE/FAILED/재시도 대기 상태로 전이
    private void reconcileOrder(String orderId, LocalDateTime now) {
        try {
            TossConfirmResponse response = tossPaymentsClient.getPaymentByOrderId(orderId);
            // DONE 상태 -> 결제/매칭/스케줄 최종 성공
            if (isDone(response.status())) {
                paymentConfirmTxService.finalizeFromReconcileSuccess(orderId, response.paymentKey(), response.approvedAt().toLocalDateTime(), response.totalAmount());
                return;
            }

            // 실패 상태 -> 주문 실패 확정 및 롤백
            if (isFailed(response.status())) {
                paymentConfirmTxService.finalizeFromReconcileFailure(orderId, response.status());
                return;
            }

            paymentConfirmTxService.handleReconcileRetryFailure(orderId, "RECONCILE_UNRESOLVED_STATUS", "재조회 결과 확정 불가 상태입니다: " + response.status(), now);
        } catch (TossServerException exception) { // 재조회 갱신
            paymentConfirmTxService.handleReconcileRetryFailure(
                    orderId,
                    exception.getProviderCode(),
                    exception.getProviderMessage(),
                    now);
        } catch (TossPaymentsException exception) { // 결제 실패
            paymentConfirmTxService.finalizeRejected(orderId, exception.getProviderCode(), exception.getProviderMessage());
        } catch (Exception exception) { // 재조회 자체 실패 -> 갱신
            log.error("결제 재조회 처리 중 예외 발생. orderId={}", orderId, exception);
            paymentConfirmTxService.handleReconcileRetryFailure(
                    orderId,
                    "RECONCILE_INTERNAL_ERROR",
                    exception.getMessage(),
                    now);
        }
    }

    // Toss 상태가 최종 승인(DONE)인지 판별
    private boolean isDone(String status) {
        return "DONE".equalsIgnoreCase(status);
    }

    // Toss 상태가 최종 실패(ABORTED/EXPIRED/CANCELED/PARTIAL_CANCELED)인지 판별
    private boolean isFailed(String status) {
        String normalized = status.toUpperCase(Locale.ROOT);
        return normalized.equals("ABORTED")
                || normalized.equals("EXPIRED")
                || normalized.equals("CANCELED")
                || normalized.equals("PARTIAL_CANCELED");
    }
}
