package com.solo.ptmatch.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;

import java.util.Objects;


@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {

    private static final String DEFAULT_SUCCESS_MESSAGE = "success";

    private final String message;
    private final T data;
    private final PageResponse pageResponse;

    private ApiResponse(String message, T data, PageResponse pageResponse) {
        this.message = message;
        this.data = data;
        this.pageResponse = pageResponse;
    }

    public static <T> ApiResponse<T> success() {
        return new ApiResponse<>(DEFAULT_SUCCESS_MESSAGE, null, null);
    }

    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>(DEFAULT_SUCCESS_MESSAGE, data, null);
    }

    public static <T> ApiResponse<T> success(T data, PageResponse pageResponse) {
        return new ApiResponse<>(DEFAULT_SUCCESS_MESSAGE, data, PageResponse.nullSafe(pageResponse));
    }
}
