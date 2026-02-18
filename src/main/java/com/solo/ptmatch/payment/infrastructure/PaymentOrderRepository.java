package com.solo.ptmatch.payment.infrastructure;

import com.solo.ptmatch.payment.domain.PaymentOrder;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentOrderRepository extends JpaRepository<PaymentOrder, Long> {

    Optional<PaymentOrder> findByOrderId(String orderId);

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
