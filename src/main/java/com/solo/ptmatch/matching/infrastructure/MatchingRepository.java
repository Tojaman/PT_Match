package com.solo.ptmatch.matching.infrastructure;

import com.solo.ptmatch.matching.domain.Matching;
import com.solo.ptmatch.matching.domain.MatchingStatus;
import com.solo.ptmatch.trainer.domain.TrainerProfile;
import com.solo.ptmatch.user.domain.User;
import org.hibernate.metamodel.mapping.WhereRestrictable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MatchingRepository extends JpaRepository<Matching, Long> {

    List<Matching> findAllByUserId(Long userId);

    @Query("SELECT DISTINCT m FROM Matching m " +
           "JOIN FETCH m.trainerProfile tp " +
           "JOIN FETCH tp.trainer " +
           "JOIN FETCH m.product " +
           "WHERE m.user.id = :userId")
    List<Matching> findAllByUserIdWithDetails(@Param("userId") Long userId);

    @Query("SELECT m FROM Matching m " +
            "JOIN FETCH m.product " +
            "WHERE m.matchingStatus = :matchingStatus " +
            "AND m.trainerProfile.id = :trainerProfileId")
    List<Matching> findAllByTrainerProfileIdAndStatusWithProduct(Long trainerProfileId, MatchingStatus matchingStatus);
}