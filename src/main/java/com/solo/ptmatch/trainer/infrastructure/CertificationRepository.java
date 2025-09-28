package com.solo.ptmatch.trainer.infrastructure;

import com.solo.ptmatch.trainer.domain.Certification;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CertificationRepository extends JpaRepository<Certification, Long> {
}
