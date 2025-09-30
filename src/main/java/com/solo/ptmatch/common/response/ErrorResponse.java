package com.solo.ptmatch.common.response;

import com.solo.ptmatch.common.exception.ErrorCode;
import com.solo.ptmatch.common.exception.GlobalException;

import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class ErrorResponse {

    private LocalDateTime errorTime;
    private ErrorCode errorCode;
    private String message;

    private ErrorResponse(ErrorCode errorCode, String message) {
        this.errorTime = LocalDateTime.now();
        this.errorCode = errorCode;
        this.message = message;
    }

    public static ErrorResponse from(GlobalException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        return new ErrorResponse(errorCode, exception.getMessage());
    }

    public static ErrorResponse of(ErrorCode errorCode, String message) {
        return new ErrorResponse(errorCode, message);
    }
}
