package com.solo.ptmatch.email.infrastructure;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "email.outbox")
public class EmailOutboxProperties {

    private long pollInterval = 5000;
    private int batchSize = 20;
    private int maxRetry = 5;
}
