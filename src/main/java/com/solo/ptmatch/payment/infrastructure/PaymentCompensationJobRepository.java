package com.solo.ptmatch.payment.infrastructure;

import com.solo.ptmatch.payment.domain.PaymentCompensationJob;
import com.solo.ptmatch.payment.domain.PaymentCompensationJobStatus;
import jakarta.persistence.LockModeType;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PaymentCompensationJobRepository extends JpaRepository<PaymentCompensationJob, Long> {

    Optional<PaymentCompensationJob> findByPaymentOrder_Id(Long paymentOrderId);

    @Query("""
            SELECT pcj.id FROM PaymentCompensationJob pcj
            WHERE pcj.status IN :statuses
              AND pcj.nextRetryAt <= :now
            ORDER BY pcj.nextRetryAt ASC
            """)
    List<Long> findExecutableJobIds(
            @Param("statuses") Collection<PaymentCompensationJobStatus> statuses,
            @Param("now") LocalDateTime now,
            Pageable pageable);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("""
            SELECT pcj FROM PaymentCompensationJob pcj
            JOIN FETCH pcj.paymentOrder po
            LEFT JOIN FETCH po.matching
            WHERE pcj.id = :jobId
            """)
    Optional<PaymentCompensationJob> findByIdWithLock(@Param("jobId") Long jobId);
}
