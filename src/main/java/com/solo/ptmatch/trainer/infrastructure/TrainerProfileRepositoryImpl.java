

package com.solo.ptmatch.trainer.infrastructure;

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
    public Map<Long, Long> countTrainersByCellRanges(SportType sportType, List<S2CellRange> ranges) {
        if (sportType == null || ranges == null || ranges.isEmpty()) {
            return Map.of();
        }

        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Object[]> query = cb.createQuery(Object[].class);
        Root<TrainerProfile> root = query.from(TrainerProfile.class);

        // SELECT s2_cell_id, COUNT(*)
        query.multiselect(
                root.get("s2CellId"),
                cb.count(root));

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
                        arr -> (Long) arr[1] // count
                ));
    }
}
