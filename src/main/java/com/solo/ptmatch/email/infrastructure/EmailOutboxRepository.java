package com.solo.ptmatch.email.infrastructure;

import com.solo.ptmatch.email.domain.EmailOutbox;
import com.solo.ptmatch.email.domain.EmailOutboxStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface EmailOutboxRepository extends JpaRepository<EmailOutbox, Long> {

    Optional<EmailOutbox> findByReferenceId(String referenceId);

    @Query("""
                SELECT e FROM EmailOutbox e
                WHERE e.status = :status
                AND e.nextRetryAt <= :now
                ORDER BY e.nextRetryAt ASC
            """)
    List<EmailOutbox> findPendingEmails(
            @Param("status") EmailOutboxStatus status,
            @Param("now") LocalDateTime now,
            Pageable pageable);

    @Modifying
    @Query("""
                UPDATE EmailOutbox e
                SET e.status = 'PROCESSING'
                WHERE e.id = :id AND e.status = 'PENDING'
            """)
    int markAsProcessing(@Param("id") Long id);
}
