package com.solo.ptmatch.payment.infrastructure.toss;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.solo.ptmatch.payment.infrastructure.toss.dto.TossConfirmRequest;
import com.solo.ptmatch.payment.infrastructure.toss.dto.TossConfirmResponse;
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
    public TossConfirmResponse confirm(String paymentKey, String orderId, int amount) {
        TossConfirmRequest request = new TossConfirmRequest(paymentKey, orderId, amount);

        try {
            TossConfirmResponse response = tossRestClient.post()
                    .uri("/v1/payments/confirm")
                    .headers(headers -> headers.setBasicAuth(tossPaymentsProperties.secretKey(), ""))
                    .body(request)
                    .retrieve()
                    .body(TossConfirmResponse.class);

            if (response == null) { // 5xx -> 재시도
                throw new TossServerException(HttpStatus.INTERNAL_SERVER_ERROR, "EMPTY_RESPONSE", "Toss 응답이 비어 있습니다.");
            }
            return response;
        } catch (RestClientResponseException exception) { // 4xx, 5xx
            throw toTossException(exception);
        } catch (RestClientException exception) { // 5xx -> 재시도
            throw new TossServerException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "NETWORK_ERROR",
                    exception.getMessage());
        }
    }

    @Override
    public TossConfirmResponse getPaymentByOrderId(String orderId) {
        try {
            TossConfirmResponse response = tossRestClient.get()
                    .uri("/v1/payments/orders/{orderId}", orderId)
                    .headers(headers -> headers.setBasicAuth(tossPaymentsProperties.secretKey(), ""))
                    .retrieve()
                    .body(TossConfirmResponse.class);

            if (response == null) { // 5xx -> 재시도
                throw new TossServerException(HttpStatus.INTERNAL_SERVER_ERROR, "EMPTY_RESPONSE", "Toss 응답이 비어 있습니다.");
            }
            return response;
        } catch (RestClientResponseException exception) { // 4xx, 5xx
            throw toTossException(exception);
        } catch (RestClientException exception) { // 5xx -> 재시도
            throw new TossServerException(
                    HttpStatus.SERVICE_UNAVAILABLE,
                    "NETWORK_ERROR",
                    exception.getMessage());
        }
    }

    private TossPaymentsException toTossException(RestClientResponseException exception) {
        TossErrorResponse errorResponse = parseErrorResponse(exception.getResponseBodyAsString());
        String providerCode = errorResponse != null && StringUtils.hasText(errorResponse.code())
                ? errorResponse.code()
                : "HTTP_" + exception.getStatusCode().value();
        String providerMessage = errorResponse != null && StringUtils.hasText(errorResponse.message())
                ? errorResponse.message()
                : exception.getResponseBodyAsString();

        if (exception.getStatusCode().is4xxClientError()) {
            return new TossPaymentsException(exception.getStatusCode(), providerCode, providerMessage);
        }
        if (exception.getStatusCode().is5xxServerError()) {
            return new TossServerException(exception.getStatusCode(), providerCode, providerMessage);
        }
        return new TossPaymentsException(exception.getStatusCode(), providerCode, providerMessage);
    }

    private TossErrorResponse parseErrorResponse(String responseBody) {
        if (!StringUtils.hasText(responseBody)) {
            return null;
        }

        try {
            return objectMapper.readValue(responseBody, TossErrorResponse.class);
        } catch (JsonProcessingException ignored) {
            return null;
        }
    }
}
