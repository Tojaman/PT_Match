package com.solo.ptmatch.payment.scheduler;

import com.solo.ptmatch.payment.application.CompensationExecutionTarget;
import com.solo.ptmatch.payment.application.PaymentConfirmTxService;
import com.solo.ptmatch.payment.application.PaymentRetryPolicy;
import com.solo.ptmatch.payment.domain.PaymentCompensationJobStatus;
import com.solo.ptmatch.payment.infrastructure.PaymentCompensationJobRepository;
import com.solo.ptmatch.payment.infrastructure.toss.TossPaymentsClient;
import com.solo.ptmatch.payment.infrastructure.toss.TossPaymentsException;
import com.solo.ptmatch.payment.infrastructure.toss.TossServerException;
import com.solo.ptmatch.payment.infrastructure.toss.dto.TossConfirmResponse;
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
public class PaymentCompensationScheduler {

    private final PaymentCompensationJobRepository paymentCompensationJobRepository;
    private final PaymentConfirmTxService paymentConfirmTxService;
    private final TossPaymentsClient tossPaymentsClient;
    private final PaymentRetryPolicy paymentRetryPolicy;

    // CANCEL_PENDING 보상작업
    @Scheduled(fixedDelayString = "${payment.retry.reconcile-interval}")
    public void processCompensationJobs() {
        LocalDateTime now = LocalDateTime.now();
        List<Long> compensationJobIds = paymentCompensationJobRepository.findExecutableJobIds(
                List.of(PaymentCompensationJobStatus.PENDING, PaymentCompensationJobStatus.RETRY_WAITING),
                now,
                PageRequest.of(0, paymentRetryPolicy.reconcileBatchSize()));

        for (Long compensationJobId : compensationJobIds) {
            processCompensationJob(compensationJobId, now);
        }
    }

    private void processCompensationJob(Long compensationJobId, LocalDateTime now) {
        CompensationExecutionTarget target = paymentConfirmTxService.getCompensationExecutionTarget(compensationJobId);
        if (target == null) {
            return;
        }

        try {
            TossConfirmResponse response = tossPaymentsClient.cancel(target.paymentKey(), "로컬 반영 실패 보상 취소");
            if (isCanceled(response.status())) {
                paymentConfirmTxService.finalizeCompensationSuccess(compensationJobId);
                return;
            }

            paymentConfirmTxService.handleCompensationRetryFailure(
                    compensationJobId,
                    "COMPENSATION_UNRESOLVED_STATUS",
                    "취소 응답 상태가 확정되지 않았습니다: " + response.status(),
                    true,
                    now);
        } catch (TossServerException exception) { // 5xx
            paymentConfirmTxService.handleCompensationRetryFailure(
                    compensationJobId,
                    exception.getProviderCode(),
                    exception.getProviderMessage(),
                    true,
                    now);
        } catch (TossPaymentsException exception) { // 4xx
            paymentConfirmTxService.handleCompensationRetryFailure(
                    compensationJobId,
                    exception.getProviderCode(),
                    exception.getProviderMessage(),
                    false,
                    now);
        } catch (Exception exception) {
            log.error("보상 취소 처리 중 예외 발생. compensationJobId={}", compensationJobId, exception);
            paymentConfirmTxService.handleCompensationRetryFailure(
                    compensationJobId,
                    "COMPENSATION_INTERNAL_ERROR",
                    exception.getMessage(),
                    true,
                    now);
        }
    }

    private boolean isCanceled(String status) {
        return "CANCELED".equalsIgnoreCase(status)
                || "PARTIAL_CANCELED".equalsIgnoreCase(status);
    }
}

