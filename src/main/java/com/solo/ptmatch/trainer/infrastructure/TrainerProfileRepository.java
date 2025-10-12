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
    // specialty가 null인 경우 inner join을 하면 specialty가 없는 트레이너는 결과에서 제외되어 조회가 되지 않는다.
    // 따라서 left outer join을 사용해야 한다.
    @Query(
            value = """
                    select distinct tp
                    from TrainerProfile tp
                    left join tp.specialties s
                    where (:specialty is null or s = :specialty)
                      and (:gymAddressKeyword is null or tp.gymAddress like concat('%', :gymAddressKeyword, '%'))
                    """,
            countQuery = """
                    select count(distinct tp)
                    from TrainerProfile tp
                    left join tp.specialties s
                    where (:specialty is null or s = :specialty)
                      and (:gymAddressKeyword is null or tp.gymAddress like concat('%', :gymAddressKeyword, '%'))
                    """
    )
    Page<TrainerProfile> searchBySpecialtyAndGymAddress(
            @Param("specialty") Specialty specialty,
            @Param("gymAddressKeyword") String gymAddressKeyword,
            Pageable pageable
    );
}
