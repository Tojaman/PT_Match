package com.solo.ptmatch.common.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 스케줄링 및 비동기 작업 스레드 풀 설정
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {

    /**
     * 통계 집계용 스레드 풀
     */
    @Bean(name = "statsExecutor")
    public ThreadPoolTaskExecutor statsExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();

        executor.setCorePoolSize(10);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(10000);

        // 스레드 이름 설정
        executor.setThreadNamePrefix("stats-");

        // 종료 정책
        executor.setWaitForTasksToCompleteOnShutdown(true); // 종료 시 작업 완료 대기
        executor.setAwaitTerminationSeconds(60); // 최대 60초 대기

        // 거부 정책 (큐 초과 시)
        // CallerRunsPolicy: 호출한 스레드에서 직접 실행 (작업 유실 방지)
        executor.setRejectedExecutionHandler(
                new java.util.concurrent.ThreadPoolExecutor.CallerRunsPolicy());

        executor.initialize();
        return executor;
    }
}
