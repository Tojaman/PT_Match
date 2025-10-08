package com.solo.ptmatch.trainer.infrastructure;

import com.solo.ptmatch.trainer.domain.TrainerFollow;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainerFollowRepository extends JpaRepository<TrainerFollow, Long> {

    // 팔로우 토글(Insert or Delete)
    Optional<TrainerFollow> findByUserIdAndTrainerProfileId(Long memberId, Long trainerProfileId);

    // 팔로우중인 트레이너 리스트 조회
    @EntityGraph(attributePaths = "trainerProfile") // N+1 방지
    List<TrainerFollow> findAllByUserId(Long memberId);

    long countByTrainerProfileId(Long trainerProfileId);
}
