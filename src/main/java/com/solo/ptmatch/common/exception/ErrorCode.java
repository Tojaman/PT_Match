package com.solo.ptmatch.common.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    INVALID_REQUEST("잘못된 요청입니다.", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("인증이 필요합니다.", HttpStatus.UNAUTHORIZED),
    FORBIDDEN("접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
    NOT_FOUND("리소스를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    CONFLICT("이미 존재하는 리소스입니다.", HttpStatus.CONFLICT),
    USER_NOT_FOUND("사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    USER_EMAIL_DUPLICATED("이미 사용 중인 이메일입니다.", HttpStatus.CONFLICT),
    TRAINER_PROFILE_NOT_FOUND("트레이너 프로필을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    INTERNAL_SERVER_ERROR("서버 내부 오류가 발생했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    PRODUCT_NOT_FOUND("상품을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    AVAILABLE_SCHEDULE_NOT_FOUND("가능한 스케줄을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    CANNOT_DELETE_RESERVED_SCHEDULE("예약된 스케줄은 삭제할 수 없습니다.", HttpStatus.BAD_REQUEST);

    private final String message;
    private final HttpStatus status;

    ErrorCode(String message, HttpStatus status) {
        this.message = message;
        this.status = status;
    }
}
