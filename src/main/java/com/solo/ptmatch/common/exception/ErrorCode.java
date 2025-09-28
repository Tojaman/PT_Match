package com.solo.ptmatch.common.exception;

import org.springframework.http.HttpStatus;

public enum ErrorCode {
    INVALID_REQUEST("COMMON_001", HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    UNAUTHORIZED("COMMON_002", HttpStatus.UNAUTHORIZED, "인증이 필요합니다."),
    FORBIDDEN("COMMON_003", HttpStatus.FORBIDDEN, "접근 권한이 없습니다."),
    NOT_FOUND("COMMON_004", HttpStatus.NOT_FOUND, "리소스를 찾을 수 없습니다."),
    CONFLICT("COMMON_005", HttpStatus.CONFLICT, "이미 존재하는 리소스입니다."),
    INTERNAL_SERVER_ERROR("COMMON_999", HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 오류가 발생했습니다.");

    private final String code;
    private final HttpStatus httpStatus;
    private final String message;

    ErrorCode(String code, HttpStatus httpStatus, String message) {
        this.code = code;
        this.httpStatus = httpStatus;
        this.message = message;
    }

    public String getCode() {
        return code;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public String getMessage() {
        return message;
    }
}
