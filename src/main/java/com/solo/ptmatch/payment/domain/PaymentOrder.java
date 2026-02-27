package com.solo.ptmatch.payment.domain;

import com.solo.ptmatch.common.BaseEntity;
import com.solo.ptmatch.matching.domain.Matching;
import com.solo.ptmatch.trainer.domain.TrainerProfile;
import com.solo.ptmatch.user.domain.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(name = "payment_orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentOrder extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_order_id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "matching_id", unique = true)
    private Matching matching;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "trainer_profile_id", nullable = false)
    private TrainerProfile trainerProfile;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PaymentProvider provider;

    @Column(name = "order_id", nullable = false, unique = true, length = 64)
    private String orderId;

    @Column(name = "payment_key", length = 200)
    private String paymentKey;

    @Column(nullable = false)
    private Integer amount;

    @Column(name = "request_message", columnDefinition = "TEXT")
    private String requestMessage;

    @Column(name = "customer_name", nullable = false)
    private String customerName;

    @Column(name = "customer_email")
    private String customerEmail;

    @Column(name = "customer_phone")
    private String customerPhone;

    @OneToMany(mappedBy = "paymentOrder", cascade = CascadeType.ALL, orphanRemoval = true)
    private final List<PaymentOrderSchedule> paymentOrderSchedules = new ArrayList<>();

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PaymentStatus status;

    @Column(name = "approved_at")
    private LocalDateTime approvedAt;

    @Column(name = "failed_code", length = 100)
    private String failedCode;

    @Column(name = "failed_message", length = 500)
    private String failedMessage;

    @Column(name = "attempt_count", nullable = false, columnDefinition = "integer default 0")
    private int attemptCount = 0;

    @Column(name = "next_retry_at")
    private LocalDateTime nextRetryAt;

    @Column(name = "resolve_deadline_at")
    private LocalDateTime resolveDeadlineAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    @Version
    @Column(name = "version", nullable = false)
    private Long version;

    private PaymentOrder(
            User user,
            TrainerProfile trainerProfile,
            PaymentProvider provider,
            String orderId,
            Integer amount,
            String requestMessage,
            String customerName,
            String customerEmail,
            String customerPhone,
            PaymentStatus status,
            LocalDateTime expiresAt) {
        this.user = user;
        this.trainerProfile = trainerProfile;
        this.provider = provider;
        this.orderId = orderId;
        this.amount = amount;
        this.requestMessage = requestMessage;
        this.customerName = customerName;
        this.customerEmail = customerEmail;
        this.customerPhone = customerPhone;
        this.status = status;
        this.expiresAt = expiresAt;
    }

    public static PaymentOrder ready(
            User user,
            TrainerProfile trainerProfile,
            String orderId,
            Integer amount,
            String requestMessage,
            String customerName,
            String customerEmail,
            String customerPhone,
            List<Long> requestedScheduleIds,
            LocalDateTime expiresAt) {

        PaymentOrder paymentOrder = new PaymentOrder(
                user,
                trainerProfile,
                PaymentProvider.TOSS,
                orderId,
                amount,
                requestMessage,
                customerName,
                customerEmail,
                customerPhone,
                PaymentStatus.READY,
                expiresAt);

        requestedScheduleIds.forEach(paymentOrder::addSchedule);
        return paymentOrder;
    }

    private void addSchedule(Long availableScheduleId) {
        this.paymentOrderSchedules.add(PaymentOrderSchedule.of(this, availableScheduleId));
    }

    public void assignMatching(Matching matching) {
        this.matching = matching;
    }

    public List<Long> getRequestedScheduleIdList() {
        return paymentOrderSchedules.stream()
                .map(PaymentOrderSchedule::getAvailableScheduleId)
                .toList();
    }

    public void markDone(String paymentKey, LocalDateTime approvedAt) {
        this.status = PaymentStatus.DONE;
        this.paymentKey = paymentKey;
        this.approvedAt = approvedAt;
        this.failedCode = null;
        this.failedMessage = null;
        clearRetryMeta();
    }

    public void markApproving(String paymentKey) {
        this.status = PaymentStatus.APPROVING;
        this.paymentKey = paymentKey;
    }

    public void markFailed(String failedCode, String failedMessage) {
        this.status = PaymentStatus.FAILED;
        this.failedCode = failedCode;
        this.failedMessage = failedMessage;
        clearRetryMeta();
    }

    public void markUnknown(String failedCode, String failedMessage, LocalDateTime nextRetryAt, LocalDateTime resolveDeadlineAt) {
        this.status = PaymentStatus.UNKNOWN;
        this.failedCode = failedCode;
        this.failedMessage = failedMessage;
        this.attemptCount = 0;
        this.nextRetryAt = nextRetryAt;
        this.resolveDeadlineAt = resolveDeadlineAt;
    }

    public void markRetryWaiting(int attemptCount, String failedCode, String failedMessage, LocalDateTime nextRetryAt) {
        this.status = PaymentStatus.UNKNOWN;
        this.attemptCount = attemptCount;
        this.failedCode = failedCode;
        this.failedMessage = failedMessage;
        this.nextRetryAt = nextRetryAt;
    }

    public void markManualReview(String failedCode, String failedMessage) {
        this.status = PaymentStatus.MANUAL_REVIEW;
        this.failedCode = failedCode;
        this.failedMessage = failedMessage;
        clearRetryMeta();
    }

    public void markCanceled() {
        this.status = PaymentStatus.CANCELED;
        this.failedCode = null;
        this.failedMessage = null;
        clearRetryMeta();
    }

    public void markExpired() {
        this.status = PaymentStatus.EXPIRED;
        this.failedCode = "PAYMENT_ORDER_EXPIRED";
        this.failedMessage = "결제 유효시간이 만료되었습니다.";
        clearRetryMeta();
    }

    public boolean isDone() {
        return status == PaymentStatus.DONE;
    }

    public boolean isTerminal() {
        return status == PaymentStatus.DONE
                || status == PaymentStatus.FAILED
                || status == PaymentStatus.CANCELED
                || status == PaymentStatus.EXPIRED;
    }

    public boolean isExpired(LocalDateTime now) {
        return expiresAt != null && now.isAfter(expiresAt);
    }

    private void clearRetryMeta() {
        this.attemptCount = 0;
        this.nextRetryAt = null;
        this.resolveDeadlineAt = null;
    }
}
