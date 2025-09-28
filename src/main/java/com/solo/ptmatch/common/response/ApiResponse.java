package com.solo.ptmatch.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.util.Objects;
import org.springframework.http.HttpStatus;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private static final String DEFAULT_SUCCESS_MESSAGE = "success";

    private final int status;
    private final String message;
    private final T data;
    private final PageResponse pageResponse;

    private ApiResponse(int status, String message, T data, PageResponse pageResponse) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.pageResponse = pageResponse;
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(HttpStatus.OK.value(), DEFAULT_SUCCESS_MESSAGE, data, null);
    }

    public static <T> ApiResponse<T> success(T data, PageResponse pageResponse) {
        return new ApiResponse<>(HttpStatus.OK.value(), DEFAULT_SUCCESS_MESSAGE, data, PageResponse.nullSafe(pageResponse));
    }

    public static <T> ApiResponse<T> of(HttpStatus status, String message, T data) {
        return new ApiResponse<>(status.value(), Objects.requireNonNullElse(message, DEFAULT_SUCCESS_MESSAGE), data, null);
    }

    public static <T> ApiResponse<T> of(HttpStatus status, String message, T data, PageResponse pageResponse) {
        return new ApiResponse<>(status.value(), Objects.requireNonNullElse(message, DEFAULT_SUCCESS_MESSAGE), data, PageResponse.nullSafe(pageResponse));
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public T getData() {
        return data;
    }

    public PageResponse getPageResponse() {
        return pageResponse;
    }
}
