package com.solo.ptmatch.trainer.infrastructure;

import com.solo.ptmatch.trainer.domain.TrainerLike;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainerLikeRepository extends JpaRepository<TrainerLike, Long> {

    // 팔로우 토글(Insert or Delete)
    Optional<TrainerLike> findByUserIdAndTrainerProfileId(Long memberId, Long trainerProfileId);

    boolean existsByUserIdAndTrainerProfileId(Long memberId, Long trainerProfileId);

    // 좋아요중인 트레이너 리스트 조회
    @EntityGraph(attributePaths = "trainerProfile")
    Page<TrainerLike> findAllByUserId(Long memberId, Pageable pageable);
}
