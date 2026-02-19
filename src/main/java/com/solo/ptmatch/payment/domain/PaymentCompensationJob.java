package com.solo.ptmatch.payment.domain;

import com.solo.ptmatch.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "payment_compensation_jobs")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentCompensationJob extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_compensation_job_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_order_id", nullable = false, unique = true)
    private PaymentOrder paymentOrder;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentCompensationJobStatus status;

    @Column(name = "attempt_count", nullable = false, columnDefinition = "integer default 0")
    private int attemptCount = 0;

    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    @Column(name = "resolve_deadline_at", nullable = false)
    private LocalDateTime resolveDeadlineAt;

    @Column(nullable = false, length = 100)
    private String reason;

    @Column(name = "trigger_source", nullable = false, length = 50)
    private String triggerSource;

    @Column(name = "last_error_code", length = 100)
    private String lastErrorCode;

    @Column(name = "last_error_message", length = 500)
    private String lastErrorMessage;

    private PaymentCompensationJob(
            PaymentOrder paymentOrder,
            String reason,
            String triggerSource,
            LocalDateTime nextRetryAt,
            LocalDateTime resolveDeadlineAt) {
        this.paymentOrder = paymentOrder;
        this.status = PaymentCompensationJobStatus.PENDING;
        this.reason = reason;
        this.triggerSource = triggerSource;
        this.nextRetryAt = nextRetryAt;
        this.resolveDeadlineAt = resolveDeadlineAt;
    }

    public static PaymentCompensationJob pending(
            PaymentOrder paymentOrder,
            String reason,
            String triggerSource,
            LocalDateTime nextRetryAt,
            LocalDateTime resolveDeadlineAt) {
        return new PaymentCompensationJob(
                paymentOrder,
                reason,
                triggerSource,
                nextRetryAt,
                resolveDeadlineAt);
    }

    public void markRetryWaiting(int nextAttemptCount, String errorCode, String errorMessage, LocalDateTime nextRetryAt) {
        this.status = PaymentCompensationJobStatus.RETRY_WAITING;
        this.attemptCount = nextAttemptCount;
        this.lastErrorCode = errorCode;
        this.lastErrorMessage = errorMessage;
        this.nextRetryAt = nextRetryAt;
    }

    public void markSucceeded() {
        this.status = PaymentCompensationJobStatus.SUCCEEDED;
        this.lastErrorCode = null;
        this.lastErrorMessage = null;
        this.nextRetryAt = null;
    }

    public void markFailedPermanent(String errorCode, String errorMessage) {
        this.status = PaymentCompensationJobStatus.FAILED_PERMANENT;
        this.lastErrorCode = errorCode;
        this.lastErrorMessage = errorMessage;
        this.nextRetryAt = null;
    }

    public boolean isTerminal() {
        return status == PaymentCompensationJobStatus.SUCCEEDED
                || status == PaymentCompensationJobStatus.FAILED_PERMANENT;
    }
}
