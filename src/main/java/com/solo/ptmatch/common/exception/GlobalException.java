package com.solo.ptmatch.common.exception;

public class GlobalException extends RuntimeException {

    private final ErrorCode errorCode;

    private GlobalException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public static GlobalException of(ErrorCode errorCode) {
        return new GlobalException(errorCode);
    }

    public static void throwError(ErrorCode errorCode) {
        throw of(errorCode);
    }

    public ErrorCode getErrorCode() {
        return errorCode;
    }
}
