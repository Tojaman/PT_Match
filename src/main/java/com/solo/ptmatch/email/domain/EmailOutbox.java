package com.solo.ptmatch.email.domain;

import com.solo.ptmatch.common.BaseEntity;
import com.solo.ptmatch.email.template.PaymentCompleteTemplate;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "email_outbox", indexes = @Index(name = "idx_email_outbox_status_retry", columnList = "status, next_retry_at"))
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class EmailOutbox extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "email_outbox_id")
    private Long id;

    @Column(name = "recipient_email", nullable = false)
    private String recipientEmail;

    @Column(name = "subject", nullable = false, length = 500)
    private String subject;

    @Column(name = "body", nullable = false, columnDefinition = "TEXT")
    private String body;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false, length = 20)
    private EmailOutboxStatus status;

    @Column(name = "retry_count", nullable = false)
    private int retryCount;

    @Column(name = "max_retry", nullable = false)
    private int maxRetry;

    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    @Column(name = "last_error_message", length = 500)
    private String lastErrorMessage;

    @Column(name = "reference_id", length = 100)
    private String referenceId;

    private EmailOutbox(String recipientEmail, String subject, String body, int maxRetry, String referenceId) {
        this.recipientEmail = recipientEmail;
        this.subject = subject;
        this.body = body;
        this.status = EmailOutboxStatus.PENDING;
        this.retryCount = 0;
        this.maxRetry = maxRetry;
        this.nextRetryAt = LocalDateTime.now();
        this.referenceId = referenceId;
    }

    public static EmailOutbox createPaymentComplete(
            String recipientEmail, String orderId,
            String customerName, Integer amount,
            LocalDateTime approvedAt, int maxRetry) {

        String subject = "[PTMatch] 결제가 완료되었습니다 - 주문번호 " + orderId;
        String body = PaymentCompleteTemplate.build(customerName, orderId, amount, approvedAt);

        return new EmailOutbox(recipientEmail, subject, body, maxRetry, orderId);
    }

    public void markSent() {
        this.status = EmailOutboxStatus.SENT;
        this.lastErrorMessage = null;
    }

    public void incrementRetry(String errorMessage) {
        this.retryCount++;
        this.lastErrorMessage = errorMessage;

        if (this.retryCount >= this.maxRetry) {
            this.status = EmailOutboxStatus.FAILED;
        } else {
            this.status = EmailOutboxStatus.PENDING;
            // 지수 백오프: 30s → 60s → 120s → 240s → ...
            long delaySeconds = 30L * (long) Math.pow(2, this.retryCount - 1);
            this.nextRetryAt = LocalDateTime.now().plusSeconds(delaySeconds);
        }
    }
}
