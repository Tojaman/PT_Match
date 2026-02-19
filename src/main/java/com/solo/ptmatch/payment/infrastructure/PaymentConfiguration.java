package com.solo.ptmatch.payment.infrastructure;

import com.solo.ptmatch.payment.infrastructure.toss.TossPaymentsProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
@EnableConfigurationProperties({PaymentProperties.class, PaymentRetryProperties.class, TossPaymentsProperties.class})
public class PaymentConfiguration {

    @Bean
    public RestClient tossRestClient(RestClient.Builder builder, TossPaymentsProperties tossPaymentsProperties) {
        return builder.baseUrl(tossPaymentsProperties.baseUrl()).build();
    }
}
