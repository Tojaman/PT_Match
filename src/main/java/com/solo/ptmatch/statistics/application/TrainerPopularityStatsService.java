package com.solo.ptmatch.statistics.application;

import com.solo.ptmatch.statistics.infrastructure.TrainerPopularityStatsRepository;
import com.solo.ptmatch.trainer.domain.SportType;
import com.solo.ptmatch.trainer.domain.TrainerPopularityScorePolicy;
import com.solo.ptmatch.trainer.infrastructure.TrainerProfileRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class TrainerPopularityStatsService {

    private static final BigDecimal ZERO_GLOBAL_AVERAGE = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    private final TrainerPopularityStatsRepository trainerPopularityStatsRepository;
    private final TrainerProfileRepository trainerProfileRepository;

    // === 동시성 문제 발생 가능성이 있어 원자적 UPDATE 사용 ===
    @Transactional
    public BigDecimal applyReviewCreated(SportType sportType, int rating) {
        trainerPopularityStatsRepository.applyReviewDelta(
                sportType.name(),
                BigDecimal.valueOf(rating),
                1L
        );
        return getGlobalAverageRating(sportType);
    }

    @Transactional
    public BigDecimal applyReviewUpdated(SportType sportType, int oldRating, int newRating) {
        trainerPopularityStatsRepository.applyReviewDelta(
                sportType.name(),
                BigDecimal.valueOf(newRating - oldRating),
                0L
        );
        return getGlobalAverageRating(sportType);
    }

    @Transactional
    public BigDecimal applyReviewDeleted(SportType sportType, int rating) {
        trainerPopularityStatsRepository.applyReviewDelta(
                sportType.name(),
                BigDecimal.valueOf(-rating),
                -1L
        );
        return getGlobalAverageRating(sportType);
    }

    @Transactional
    public void recalculateAllPopularityScores() {
        trainerProfileRepository.recalculateAllPopularityScores(TrainerPopularityScorePolicy.BAYESIAN_M);
    }

    private BigDecimal getGlobalAverageRating(SportType sportType) {
        return trainerPopularityStatsRepository.findGlobalAverageRatingBySportType(sportType)
                .orElse(ZERO_GLOBAL_AVERAGE);
    }
}
