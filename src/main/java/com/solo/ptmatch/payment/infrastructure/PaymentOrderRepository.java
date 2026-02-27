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
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, Long> {

    Optional<PaymentOrder> findByOrderId(String orderId);

    @Query("""
            SELECT po FROM PaymentOrder po
            WHERE po.status = :status
                AND po.nextRetryAt IS NOT NULL
                AND po.nextRetryAt <= :now
            ORDER BY po.nextRetryAt ASC
            """)
    List<PaymentOrder> findRetryTargetOrders(
                    @Param("status") PaymentStatus status,
                    @Param("now") LocalDateTime now,
                    Pageable pageable);

    @Query("""
            SELECT po FROM PaymentOrder po
            WHERE po.status = :status
                AND po.updatedAt <= :staleBefore
            ORDER BY po.updatedAt ASC
            """)
    List<PaymentOrder> findStaleOrders(
                    @Param("status") PaymentStatus status,
                    @Param("staleBefore") LocalDateTime staleBefore,
                    Pageable pageable);

    @Query("""
            SELECT po.orderId FROM PaymentOrder po
            WHERE po.status = :status
                AND po.expiresAt <= :now
            ORDER BY po.expiresAt ASC
            """)
    List<String> findExpiredOrderIds(
                    @Param("status") PaymentStatus status,
                    @Param("now") LocalDateTime now,
                    Pageable pageable);

    @Modifying
    @Query(value = """
            UPDATE payment_orders
            SET status = 'EXPIRED',
                failed_code = 'PAYMENT_ORDER_EXPIRED',
                failed_message = '결제 유효시간이 만료되었습니다.',
                attempt_count = 0,
                next_retry_at = NULL,
                resolve_deadline_at = NULL,
                updated_at = :now
            WHERE status = 'READY'
            AND expires_at <= :now
            """, nativeQuery = true)
    int bulkExpireReadyOrders(@Param("now") LocalDateTime now);
}
