package com.solo.ptmatch.common.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

@Aspect
@Component
public class ExecutionTimeLogger {

    private final Logger logger = LoggerFactory.getLogger(this.getClass());

    @Around("@annotation(com.solo.ptmatch.common.aop.LogExecutionTime)")
    public Object logExecutionTime(ProceedingJoinPoint joinPoint) throws Throwable {
        
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();

        // 대상 메소드 실행
        Object result = joinPoint.proceed();

        stopWatch.stop();

        String methodName = joinPoint.getSignature().getName();
        logger.info("메소드 '{}' 실행 시간: {} ms", methodName, stopWatch.getTotalTimeMillis());

        return result;
    }
}