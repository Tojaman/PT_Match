package com.solo.ptmatch.common.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import java.util.concurrent.ConcurrentHashMap;

@Aspect
@Component
public class ExecutionTimeLogger {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());
    private final ConcurrentHashMap<String, Long> methodCallCount = new ConcurrentHashMap<>();

    @Around("@annotation(com.solo.ptmatch.common.aop.LogExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {

        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // 대상 메소드 실행
        Object result = joinPoint.proceed();

        stopWatch.stop();

        String methodName = joinPoint.getSignature().getName();
        methodCallCount.merge(methodName, 1L, Long::sum);

        logger.info("메소드 '{}' 실행 시간: {} ms", methodName, stopWatch.getTotalTimeMillis());
//        logger.info("메소드 '{}' 호출 횟수: {}", methodName, methodCallCount.get(methodName));

        return result;
    }
}