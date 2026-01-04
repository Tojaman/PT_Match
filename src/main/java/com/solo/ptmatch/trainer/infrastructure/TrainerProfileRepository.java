package com.solo.ptmatch.trainer.infrastructure;

import com.solo.ptmatch.trainer.domain.Specialty;
import com.solo.ptmatch.trainer.domain.TrainerProfile;

import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TrainerProfileRepository extends JpaRepository<TrainerProfile, Long> {

    Optional<TrainerProfile> findByUserId(Long trainerId);

    Page<TrainerProfile> findByGymName(String gymName, Pageable pageable);

    // Bounding Box 반경 검색
    List<TrainerProfile> findByGymLatitudeBetweenAndGymLongitudeBetween(
            double minLat, double maxLat,
            double minLng, double maxLng);

    // 위치 자동완성용 헬스장 이름 prefix 검색
    List<TrainerProfile> findByGymNameStartingWithOrderByGymName(String gymNamePrefix);

    // ST_DWithin 기반 반경 검색
    @Query(value = """
            SELECT * FROM trainer_profiles t
            WHERE ST_DWithin(
                t.location,
                ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography,
                :radiusMeters
            )
            ORDER BY ST_Distance(
                t.location,
                ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography
            )
            """, countQuery = """
            SELECT COUNT(*) FROM trainer_profiles t
            WHERE ST_DWithin(
                t.location,
                ST_SetSRID(ST_MakePoint(:lon, :lat), 4326)::geography,
                :radiusMeters
            )
            """, nativeQuery = true)
    Page<TrainerProfile> findNearby(
            @Param("lon") double longitude,
            @Param("lat") double latitude,
            @Param("radiusMeters") double radiusMeters,
            Pageable pageable);

    // JPQL 기반 법정동 내 트레이너 검색
    @Query(value = """
            SELECT t FROM TrainerProfile t
            JOIN LegalDistrict ld ON function('ST_Contains', ld.boundary, t.location) = true
            WHERE ld.code = :districtCode
            """)
    Page<TrainerProfile> findByDistrictCode(
            @Param("districtCode") String districtCode,
            Pageable pageable);

    @Query(value = """
            SELECT t FROM TrainerProfile t
            WHERE t.districtCode = :districtCode
            """)
    Page<TrainerProfile> findByDistrictCodedd(
            @Param("districtCode") String districtCode,
            Pageable pageable);

    // 줌 레벨에 따른 법정동별 트레이너 수 집계
    @Query(value = """
            SELECT ld.code as districtCode, ld.name as districtName,
                   ST_Y(ST_Centroid(ld.boundary)) as latitude,
                   ST_X(ST_Centroid(ld.boundary)) as longitude,
                   COUNT(t.trainer_profile_id) as trainerCount
            FROM legal_districts ld
            JOIN trainer_profiles t ON ld.code = t.district_code
            WHERE ST_Intersects(
                ld.boundary,
                ST_MakeEnvelope(:minLon, :minLat, :maxLon, :maxLat, 4326)
            )
            GROUP BY ld.code, ld.name
            """, nativeQuery = true)
    List<MapClusterProjection> findDistrictClustersByDistrictCode(
            @Param("minLon") double minLon,
            @Param("minLat") double minLat,
            @Param("maxLon") double maxLon,
            @Param("maxLat") double maxLat);

}
