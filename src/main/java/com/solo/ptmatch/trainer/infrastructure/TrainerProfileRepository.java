package com.solo.ptmatch.trainer.infrastructure;

import com.solo.ptmatch.trainer.domain.Specialty;
import com.solo.ptmatch.trainer.domain.TrainerProfile;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TrainerProfileRepository extends JpaRepository<TrainerProfile, Long> {

    Optional<TrainerProfile> findByUserId(Long trainerId);

    // 조건: 전문 분야 or 주소로 트레이너 검색
    // 정렬: 팔로워순/별점순 + 오름차순/내림차순(Pageable 객체 내부에 Sort 객체 포함) -> order by 쿼리 JPA가 동적으로 생성
    @Query("""
            select tp
            from TrainerProfile tp
            where (:specialty is null or tp.specialty = :specialty)
              and (:gymAddressKeyword is null or tp.gymAddress like concat('%', :gymAddressKeyword, '%'))
            """)
    Page<TrainerProfile> searchBySpecialtyAndGymAddress(
            @Param("specialty") Specialty specialty,
            @Param("gymAddressKeyword") String gymAddressKeyword,
            Pageable pageable
    );
}
