package com.solo.ptmatch.payment.infrastructure;

import com.solo.ptmatch.payment.domain.PaymentOrder;
import com.solo.ptmatch.payment.domain.PaymentStatus;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, Long> {

    Optional<PaymentOrder> findByOrderId(String orderId);

    @Query("""
            SELECT po FROM PaymentOrder po
            JOIN FETCH po.user
            LEFT JOIN FETCH po.matching
            WHERE po.orderId = :orderId
            """)
    Optional<PaymentOrder> findByOrderIdWithUserAndMatching(@Param("orderId") String orderId);

    @Query("""
            SELECT po.orderId FROM PaymentOrder po
            WHERE po.status = :status
              AND po.nextRetryAt IS NOT NULL
              AND po.nextRetryAt <= :now
            ORDER BY po.nextRetryAt ASC
            """)
    List<String> findRetryTargetOrderIds(
            @Param("status") PaymentStatus status,
            @Param("now") LocalDateTime now,
            Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT po FROM PaymentOrder po
            JOIN FETCH po.user
            JOIN FETCH po.trainerProfile
            LEFT JOIN FETCH po.matching
            WHERE po.orderId = :orderId
            """)
    Optional<PaymentOrder> findByOrderIdWithLock(@Param("orderId") String orderId);
}
