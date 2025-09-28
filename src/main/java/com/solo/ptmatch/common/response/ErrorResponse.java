package com.solo.ptmatch.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.solo.ptmatch.common.exception.GlobalException;
import java.time.LocalDateTime;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private static final String DEFAULT_ERROR_CODE = "INTERNAL_SERVER_ERROR";

    private final LocalDateTime errorTime;
    private final String code;
    private final String message;
    private final Object data;

    private ErrorResponse(LocalDateTime errorTime, String code, String message, Object data) {
        this.errorTime = errorTime;
        this.code = code;
        this.message = message;
        this.data = data;
    }

    public static ErrorResponse from(GlobalException exception) {
        Objects.requireNonNull(exception, "exception must not be null");
        return new ErrorResponse(LocalDateTime.now(), DEFAULT_ERROR_CODE, exception.getMessage(), null);
    }

    public static ErrorResponse of(String code, String message, Object data) {
        return new ErrorResponse(LocalDateTime.now(), Objects.requireNonNullElse(code, DEFAULT_ERROR_CODE), message, data);
    }

    public LocalDateTime getErrorTime() {
        return errorTime;
    }

    public String getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public Object getData() {
        return data;
    }
}
