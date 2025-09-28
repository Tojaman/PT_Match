package com.solo.ptmatch.common.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.solo.ptmatch.common.exception.ErrorCode;
import com.solo.ptmatch.common.exception.GlobalException;
import java.time.LocalDateTime;
import java.util.Objects;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private static final String DEFAULT_ERROR_CODE = ErrorCode.INTERNAL_SERVER_ERROR.getCode();

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
        ErrorCode errorCode = exception.getErrorCode();
        Object errorData = exception.getData().orElse(null);
        String resolvedCode = errorCode != null ? errorCode.getCode() : DEFAULT_ERROR_CODE;
        String resolvedMessage = exception.getMessage();
        return new ErrorResponse(LocalDateTime.now(), resolvedCode, resolvedMessage, errorData);
    }

    public static ErrorResponse of(ErrorCode errorCode, String message, Object data) {
        ErrorCode safeCode = errorCode == null ? ErrorCode.INTERNAL_SERVER_ERROR : errorCode;
        String resolvedMessage = message == null || message.isBlank() ? safeCode.getMessage() : message;
        return new ErrorResponse(LocalDateTime.now(), safeCode.getCode(), resolvedMessage, data);
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
