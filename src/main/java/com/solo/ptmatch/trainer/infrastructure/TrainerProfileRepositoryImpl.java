package com.solo.ptmatch.trainer.infrastructure;

import com.solo.ptmatch.common.util.S2CellRange;
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

@Slf4j
@Repository
public class TrainerProfileRepositoryImpl implements TrainerProfileRepositoryCustom {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public List<LatLngProjection> findLatLngByS2CellRangesCriteria(List<S2CellRange> ranges) {
        if (ranges == null || ranges.isEmpty()) {
            return List.of();
        }

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<LatLngProjection> query = cb.createQuery(LatLngProjection.class);
        Root<TrainerProfile> root = query.from(TrainerProfile.class);

        // SELECT gym_latitude, gym_longitude
        query.select(cb.construct(
                LatLngProjection.class,
                root.get("gymLatitude"),
                root.get("gymLongitude")));

        // WHERE (s2_cell_id BETWEEN min1 AND max1) OR (s2_cell_id BETWEEN min2 AND
        // max2) OR ...
        List<Predicate> orPredicates = new ArrayList<>();
        for (S2CellRange range : ranges) {
            orPredicates.add(cb.between(
                    root.get("s2CellId"),
                    range.minId(),
                    range.maxId()));
        }
        query.where(cb.or(orPredicates.toArray(new Predicate[0])));

        return entityManager.createQuery(query).getResultList();
    }
}
