package com.solo.ptmatch.payment.infrastructure.toss;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.solo.ptmatch.payment.infrastructure.toss.dto.TossCancelRequest;
import com.solo.ptmatch.payment.infrastructure.toss.dto.TossConfirmRequest;
import com.solo.ptmatch.payment.infrastructure.toss.dto.TossPaymentResponse;
import com.solo.ptmatch.payment.infrastructure.toss.dto.TossErrorResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
@RequiredArgsConstructor
public class TossPaymentsApiClient implements TossPaymentsClient {

    private final RestClient tossRestClient;
    private final TossPaymentsProperties tossPaymentsProperties;
    private final ObjectMapper objectMapper;

    @Override
    public TossPaymentResponse confirm(String paymentKey, String orderId, int amount) {
        TossConfirmRequest request = new TossConfirmRequest(paymentKey, orderId, amount);

        try {
            return tossRestClient.post()
                    .uri("/v1/payments/confirm")
                    .headers(headers -> headers.setBasicAuth(tossPaymentsProperties.secretKey(), ""))
                    .body(request)
                    .retrieve()
                    .body(TossPaymentResponse.class);
        } catch (RestClientResponseException exception) { // 4xx, 5xx
            throw toTossException(exception);
        } catch (RestClientException exception) { // 네트워크 오류
            throw new TossServerException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "NETWORK_ERROR",
                    exception.getMessage());
        }
    }

    @Override
    public TossPaymentResponse getPaymentByOrderId(String orderId) {
        try {
            return tossRestClient.get()
                    .uri("/v1/payments/orders/{orderId}", orderId)
                    .headers(headers -> headers.setBasicAuth(tossPaymentsProperties.secretKey(), ""))
                    .retrieve()
                    .body(TossPaymentResponse.class);
        } catch (RestClientResponseException exception) { // 4xx, 5xx
            throw toTossException(exception);
        } catch (RestClientException exception) { // 네트워크 오류
            throw new TossServerException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "NETWORK_ERROR",
                    exception.getMessage());
        }
    }

    @Override
    public TossPaymentResponse cancel(String paymentKey, String cancelReason) {
        TossCancelRequest request = new TossCancelRequest(cancelReason);

        try {
            return tossRestClient.post()
                    .uri("/v1/payments/{paymentKey}/cancel", paymentKey)
                    .headers(headers -> headers.setBasicAuth(tossPaymentsProperties.secretKey(), ""))
                    .body(request)
                    .retrieve()
                    .body(TossPaymentResponse.class);
        } catch (RestClientResponseException exception) {
            throw toTossException(exception);
        } catch (RestClientException exception) {
            throw new TossServerException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "NETWORK_ERROR",
                    exception.getMessage());
        }
    }

    private TossPaymentsException toTossException(RestClientResponseException exception) {
        TossErrorResponse errorResponse = parseErrorResponse(exception);

        if (exception.getStatusCode().is5xxServerError()) {
            return new TossServerException(exception.getStatusCode(), errorResponse.code(), errorResponse.message());
        }
        return new TossPaymentsException(exception.getStatusCode(), errorResponse.code(), errorResponse.message());
    }

    private TossErrorResponse parseErrorResponse(RestClientResponseException exception) {
        try {
            if (StringUtils.hasText(exception.getResponseBodyAsString())) {
                return objectMapper.readValue(exception.getResponseBodyAsString(), TossErrorResponse.class);
            }
        } catch (JsonProcessingException ignored) {
        }
        return new TossErrorResponse("HTTP_" + exception.getStatusCode().value(), exception.getResponseBodyAsString());
    }
}
