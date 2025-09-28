package com.solo.ptmatch.common.exception;

import java.util.Optional;
import java.util.Objects;

public class GlobalException extends RuntimeException {

    private final ErrorCode errorCode;
    private final transient Object data;

    private GlobalException(ErrorCode errorCode, Object data, String message) {
        super(resolveMessage(errorCode, message));
        this.errorCode = Objects.requireNonNull(errorCode, "errorCode must not be null");
        this.data = data;
    }

    public static GlobalException of(ErrorCode errorCode) {
        return new GlobalException(errorCode, null, null);
    }

    public static GlobalException of(ErrorCode errorCode, Object data) {
        return new GlobalException(errorCode, data, null);
    }

    public static GlobalException of(ErrorCode errorCode, String message, Object data) {
        return new GlobalException(errorCode, data, message);
    }

    public static void throwError(ErrorCode errorCode) {
        throw of(errorCode);
    }

    public static void throwError(ErrorCode errorCode, Object data) {
        throw of(errorCode, data);
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }

    public Optional<Object> getData() {
        return Optional.ofNullable(data);
    }

    private static String resolveMessage(ErrorCode errorCode, String message) {
        if (message == null || message.isBlank()) {
            return errorCode.getMessage();
        }
        return message;
    }
}
