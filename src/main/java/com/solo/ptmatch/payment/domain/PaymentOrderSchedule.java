package com.solo.ptmatch.payment.domain;

import com.solo.ptmatch.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Table(
        name = "payment_order_schedules",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_payment_order_schedule", columnNames = {"payment_order_id", "available_schedule_id"})
        })
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PaymentOrderSchedule extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "payment_order_schedule_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "payment_order_id", nullable = false)
    private PaymentOrder paymentOrder;

    @Column(name = "available_schedule_id", nullable = false)
    private Long availableScheduleId;

    private PaymentOrderSchedule(PaymentOrder paymentOrder, Long availableScheduleId) {
        this.paymentOrder = paymentOrder;
        this.availableScheduleId = availableScheduleId;
    }

    public static PaymentOrderSchedule of(PaymentOrder paymentOrder, Long availableScheduleId) {
        return new PaymentOrderSchedule(paymentOrder, availableScheduleId);
    }
}
