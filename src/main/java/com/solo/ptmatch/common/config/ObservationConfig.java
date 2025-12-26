package com.solo.ptmatch.common.config;

import io.micrometer.observation.ObservationPredicate;
import io.micrometer.observation.ObservationRegistry;
import io.micrometer.observation.aop.ObservedAspect;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.observation.ServerRequestObservationContext;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
public class ObservationConfig {

    @Bean
    public ObservedAspect observedAspect(ObservationRegistry observationRegistry) {
        return new ObservedAspect(observationRegistry);
    }

    @Bean
    ObservationPredicate noPrometheus() {
        return (name, context) -> {
            // 1. 서버 요청 컨텍스트에서 확인
            if (context instanceof ServerRequestObservationContext serverContext) {
                return !serverContext.getCarrier().getRequestURI().contains("/actuator/prometheus");
            }

            // 2. 다른 컨텍스트(예: 보안 필터)에서도 현재 요청 URI를 확인하여 제외
            try {
                RequestAttributes attrs = RequestContextHolder.getRequestAttributes();
                if (attrs instanceof ServletRequestAttributes servletAttrs) {
                    String uri = servletAttrs.getRequest().getRequestURI();
                    if (uri != null && uri.contains("/actuator/prometheus")) {
                        return false;
                    }
                }
            } catch (Exception e) {
                // 상항에 따라 에러 발생 시 무시하고 진행
            }

            return true;
        };
    }
}
