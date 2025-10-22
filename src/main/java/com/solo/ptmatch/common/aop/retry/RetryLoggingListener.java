package com.solo.ptmatch.common.aop.retry;

import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component("retryLoggingListener")
public class RetryLoggingListener implements RetryListener {

    @Override
    public <T, E extends Throwable> void onError(
            RetryContext context,
            RetryCallback<T, E> callback,
            Throwable throwable
    ) {
        int attempt = context.getRetryCount() + 1;
        String contextName = context.getAttribute(RetryContext.NAME) != null
                ? context.getAttribute(RetryContext.NAME).toString()
                : callback.getClass().getSimpleName();
        log.warn(
                "재시도 {}회차: context={}, 예외={}, 스레드={}",
                attempt,
                contextName,
                throwable.getClass().getSimpleName(),
                Thread.currentThread().getName()
        );
    }
}
