package com.solo.ptmatch.trainer.infrastructure;

import com.solo.ptmatch.trainer.domain.SportType;
import com.solo.ptmatch.trainer.domain.TrainerProfile;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface TrainerProfileRepository extends JpaRepository<TrainerProfile, Long>, TrainerProfileRepositoryCustom {

    Optional<TrainerProfile> findByUserId(Long trainerId);

    Page<TrainerProfile> findByFacilityName(String facilityName, Pageable pageable);

    // 위치 자동완성용 시설 이름 prefix 검색
    List<TrainerProfile> findByFacilityNameStartingWith(String facilityNamePrefix);

    // S2 Cell ID 목록으로 트레이너 조회 (종목별)
    List<TrainerProfile> findBySportTypeAndS2CellIdIn(SportType sportType, List<Long> cellIds);

    // S2 Cell ID 목록으로 트레이너 조회 (성능 비교용)
    List<TrainerProfile> findByS2CellIdIn(List<Long> cellIds);

    List<TrainerProfile> findByIdIn(List<Long> ids);

    @Modifying
    @Query("""
            update TrainerProfile t
            set t.likesCount = t.likesCount + :delta
            where t.id = :trainerProfileId
              and (:delta >= 0 or t.likesCount > 0)
            """)
    int updateLikeCountAtomically(
            @Param("trainerProfileId") Long trainerProfileId,
            @Param("delta") int delta
    );

    // 위치 기반 인기 트레이너 조회 (인기도 점수 정렬)
    @Query(value = """
            SELECT * FROM trainer_profiles t
            WHERE t.s2_cell_id IN (:s2CellIds)
              AND t.sport_type = :sportType
            ORDER BY t.popularity_score DESC
            LIMIT :limit
            """, nativeQuery = true)
    List<TrainerProfile> findPopularTrainersNearbyByS2CellIdInV2(
            @Param("sportType") String sportType,
            @Param("s2CellIds") List<Long> s2CellIds,
            @Param("limit") int limit
    );

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
