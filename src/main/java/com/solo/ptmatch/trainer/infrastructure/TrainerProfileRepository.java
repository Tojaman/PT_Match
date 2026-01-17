package com.solo.ptmatch.trainer.infrastructure;

import com.solo.ptmatch.trainer.domain.TrainerProfile;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TrainerProfileRepository extends JpaRepository<TrainerProfile, Long>, TrainerProfileRepositoryCustom {

    Optional<TrainerProfile> findByUserId(Long trainerId);

    Page<TrainerProfile> findByGymName(String gymName, Pageable pageable);

    // Bounding Box 반경 검색
    List<TrainerProfile> findByGymLatitudeBetweenAndGymLongitudeBetween(
            double minLat, double maxLat,
            double minLng, double maxLng);

    // 위치 자동완성용 헬스장 이름 prefix 검색
    List<TrainerProfile> findByGymNameStartingWithOrderByGymName(String gymNamePrefix);

    // S2 Cell ID 목록으로 트레이너 조회 (성능 비교용)
    List<TrainerProfile> findByS2CellIdIn(List<Long> cellIds);

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

    // ST_SnapToGrid 기반 클러스터링 (BETWEEN 조건 + PostGIS ST_SnapToGrid 함수)
    @Query(value = """
            SELECT
                gridX,
                gridY,
                AVG(gym_latitude) as latitude,
                AVG(gym_longitude) as longitude,
                COUNT(*) as trainerCount
            FROM (
                SELECT
                    gym_latitude,
                    gym_longitude,
                    ST_X(ST_SnapToGrid(location, :gridSize)) as gridX,
                    ST_Y(ST_SnapToGrid(location, :gridSize)) as gridY
                FROM trainer_profiles
                WHERE gym_latitude BETWEEN :minLat AND :maxLat
                  AND gym_longitude BETWEEN :minLon AND :maxLon
            ) sub
            GROUP BY gridX, gridY
            """, nativeQuery = true)
    List<GridClusterProjection> findSnapToGridClustersBetween(
            @Param("minLon") double minLon,
            @Param("minLat") double minLat,
            @Param("maxLon") double maxLon,
            @Param("maxLat") double maxLat,
            @Param("gridSize") double gridSize);
}
