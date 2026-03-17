package com.solo.ptmatch.statistics.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrainerPopularityScoreScheduler {

    private final TrainerPopularityStatsService trainerPopularityStatsService;

    // 매시단 popularity_score 재계산
    @Scheduled(cron = "${statistics.popularity.cron:0 0 * * * *}")
    public void recalculateAllPopularityScores() {
        try {
            trainerPopularityStatsService.recalculateAllPopularityScores();
        } catch (Exception e) {
            log.error("전체 popularity_score 재계산에 실패했습니다.", e);
        }
    }
}
