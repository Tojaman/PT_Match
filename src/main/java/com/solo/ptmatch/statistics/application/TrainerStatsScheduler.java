package com.solo.ptmatch.statistics.application;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDate;

@Slf4j
@Component
@RequiredArgsConstructor
public class TrainerStatsScheduler {

    private final TrainerMonthlyStatsService statsService;

    // @Scheduled(cron = "0/30 * * * * *")
    @Scheduled(cron = "0 0 2 * * *") // 매일 새벽 2시
    public void aggregateDailyStats() {
        LocalDate today = LocalDate.now();
        int year = today.getYear();
        int month = today.getMonthValue();

        try {
            statsService.aggregateAllTrainersMonthlyStats(year, month);
        } catch (Exception e) {
            log.error("통계 실패: {}", e.getMessage(), e);
        }
    }
}
