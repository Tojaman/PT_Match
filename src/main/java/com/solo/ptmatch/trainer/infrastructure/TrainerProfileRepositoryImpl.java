
package com.solo.ptmatch.trainer.infrastructure;

import com.google.common.geometry.S2CellId;
import com.solo.ptmatch.common.util.S2CellRange;
import com.solo.ptmatch.trainer.domain.SportType;
import com.solo.ptmatch.trainer.domain.TrainerProfile;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class TrainerProfileRepositoryImpl implements TrainerProfileRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public Map<Long, CellStat> countTrainersByCellRanges(SportType sportType, List<S2CellRange> ranges) {
        if (sportType == null || ranges == null || ranges.isEmpty()) {
            return Map.of();
        }

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<TrainerProfile> root = query.from(TrainerProfile.class);

        // SELECT s2_cell_id, COUNT(*), SUM(latitude), SUM(longitude)
        query.multiselect(
                root.get("s2CellId"),
                cb.count(root),
                cb.sum(root.get("latitude")),
                cb.sum(root.get("longitude")));

        // WHERE sport_type = :sportType AND ((s2_cell_id BETWEEN min1 AND max1) OR
        // (s2_cell_id BETWEEN min2 AND max2) ...)
        List<Predicate> orPredicates = new ArrayList<>();
        for (S2CellRange range : ranges) {
            orPredicates.add(cb.between(
                    root.get("s2CellId"),
                    range.minId(),
                    range.maxId()));
        }

        // sportType 조건과 range 조건을 AND로 결합
        Predicate sportTypePredicate = cb.equal(root.get("sportType"), sportType);
        Predicate rangePredicate = cb.or(orPredicates.toArray(new Predicate[0]));
        query.where(cb.and(sportTypePredicate, rangePredicate));

        query.groupBy(root.get("s2CellId"));

        // Map으로 변환
        return entityManager.createQuery(query).getResultList().stream()
                .collect(Collectors.toMap(
                        arr -> (Long) arr[0], // s2_cell_id
                        arr -> CellStat.of(
                                (Long) arr[1],
                                arr[2] == null ? 0d : ((Number) arr[2]).doubleValue(),
                                arr[3] == null ? 0d : ((Number) arr[3]).doubleValue())
                ));
    }

    @Override
    public List<TrainerProfile> findTrainersByCellRanges(SportType sportType, List<S2CellRange> ranges) {
        if (sportType == null || ranges == null || ranges.isEmpty()) {
            return List.of();
        }

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<TrainerProfile> query = cb.createQuery(TrainerProfile.class);
        Root<TrainerProfile> root = query.from(TrainerProfile.class);

        // SELECT *
        query.select(root);

        // WHERE sport_type = :sportType AND ((s2_cell_id BETWEEN min1 AND max1) OR ...)
        List<Predicate> orPredicates = new ArrayList<>();
        for (S2CellRange range : ranges) {
            orPredicates.add(cb.between(
                    root.get("s2CellId"),
                    range.minId(),
                    range.maxId()));
        }

        Predicate sportTypePredicate = cb.equal(root.get("sportType"), sportType);
        Predicate rangePredicate = cb.or(orPredicates.toArray(new Predicate[0]));
        query.where(cb.and(sportTypePredicate, rangePredicate));

        return entityManager.createQuery(query).getResultList();
    }

    @Override
    public List<TrainerProfile> findTrainersByClusterCellCursor(SportType sportType, long clusterCellId, long cursor, int limit) {
        if (sportType == null || limit <= 0) {
            return List.of();
        }

        S2CellId clusterCell = new S2CellId(clusterCellId);
        long rangeMin = clusterCell.rangeMin().id();
        long rangeMax = clusterCell.rangeMax().id();

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<TrainerProfile> query = cb.createQuery(TrainerProfile.class);
        Root<TrainerProfile> root = query.from(TrainerProfile.class);

        query.select(root);

        Predicate sportTypePredicate = cb.equal(root.get("sportType"), sportType);
        Predicate rangePredicate = cb.between(root.get("s2CellId"), rangeMin, rangeMax);
        Predicate cursorPredicate = cb.greaterThan(root.get("id"), cursor);
        query.where(cb.and(sportTypePredicate, rangePredicate, cursorPredicate));
        query.orderBy(cb.asc(root.get("id")));

        return entityManager.createQuery(query)
                .setMaxResults(limit)
                .getResultList();
    }
}
