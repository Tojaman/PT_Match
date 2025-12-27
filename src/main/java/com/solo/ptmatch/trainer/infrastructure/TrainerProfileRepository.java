package com.solo.ptmatch.trainer.infrastructure;

import com.solo.ptmatch.trainer.domain.Specialty;
import com.solo.ptmatch.trainer.domain.TrainerProfile;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TrainerProfileRepository extends JpaRepository<TrainerProfile, Long> {

    Optional<TrainerProfile> findByUserId(Long trainerId);

    // 헬스장 이름 정확 매칭
    Page<TrainerProfile> findByGymName(String gymName, Pageable pageable);

    // Bounding Box 반경 검색
    Page<TrainerProfile> findByGymLatitudeBetweenAndGymLongitudeBetween(
        double minLat, double maxLat,
        double minLng, double maxLng,
        Pageable pageable);

    // 위치 자동완성용 헬스장 이름 prefix 검색
    List<TrainerProfile> findByGymNameStartingWithOrderByGymName(String gymNamePrefix);
}
