package com.solo.ptmatch.trainer.infrastructure;

import com.solo.ptmatch.trainer.domain.Certification;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

public interface CertificationRepository extends JpaRepository<Certification, Long> {

    List<Certification> findAllByTrainerProfileId(Long trainerProfileId);

    // 트레이너 자격증 맞는지 검증
    Optional<Certification> findByIdAndTrainerProfileId(Long certificationId, Long trainerProfileId);

    // 트레이너 프로필 삭제시 관련 자격증 테이블 일괄 삭제
    void deleteByTrainerProfileId(Long trainerProfileId);
}
