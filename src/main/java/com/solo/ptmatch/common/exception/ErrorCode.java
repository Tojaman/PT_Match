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
    SCHEDULE_ALREADY_RESERVED("이미 예약이 진행 중인 스케줄입니다.", HttpStatus.CONFLICT),
    CANNOT_DELETE_RESERVED_SCHEDULE("예약된 스케줄은 삭제할 수 없습니다.", HttpStatus.BAD_REQUEST),
    MATCHING_NOT_FOUND("매칭을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    PAYMENT_ORDER_NOT_FOUND("결제 주문을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    PAYMENT_AMOUNT_MISMATCH("결제 금액이 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
    PAYMENT_ORDER_EXPIRED("결제 주문이 만료되었습니다.", HttpStatus.CONFLICT),
    PAYMENT_ALREADY_PROCESSED("이미 처리된 결제 주문입니다.", HttpStatus.CONFLICT),
    PAYMENT_PROVIDER_ERROR("결제사 연동 중 오류가 발생했습니다.", HttpStatus.BAD_GATEWAY),
    REVIEW_ALREADY_EXISTS("이미 리뷰가 작성된 매칭입니다.", HttpStatus.CONFLICT),
    REVIEW_NOT_FOUND("유저의 리뷰를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    SELF_PRODUCT_LIKE_NOT_ALLOWED("본인이 등록한 상품에는 좋아요를 누를 수 없습니다.", HttpStatus.BAD_REQUEST),
    PRESIGNED_URL_GENERATION_FAILED("파일 업로드 URL을 생성할 수 없습니다.", HttpStatus.SERVICE_UNAVAILABLE),
    PRODUCT_LIKE_RETRY_FAILED("좋아요 요청이 반복해서 실패했습니다. 잠시 후 다시 시도해 주세요.", HttpStatus.SERVICE_UNAVAILABLE),
    SCHEDULE_DUPLICATED("해당 시간에 이미 등록된 스케줄이 존재합니다.", HttpStatus.CONFLICT),
    INVALID_TOKEN("유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_NOT_FOUND("리프레시 토큰을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    PASSWORD_NOT_MATCH("비밀번호가 일치하지 않습니다.", HttpStatus.UNAUTHORIZED),
    PROGRAM_NOT_FOUND("프로그램을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    MATCHING_SCHEDULE_NOT_FOUND("매칭 스케줄을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    CHAT_ROOM_NOT_FOUND("채팅방을 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    CHAT_FORBIDDEN("해당 채팅방에 접근 권한이 없습니다.", HttpStatus.FORBIDDEN),
    CHAT_PARTNER_NOT_FOUND("대상 사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    CHAT_INVALID_CURSOR("유효하지 않은 채팅 커서입니다.", HttpStatus.BAD_REQUEST),
    CHAT_MESSAGE_TOO_LONG("메시지 길이는 1000자를 초과할 수 없습니다.", HttpStatus.BAD_REQUEST);

    private final String message;
    private final HttpStatus status;

    ErrorCode(String message, HttpStatus status) {
        this.message = message;
        this.status = status;
    }
}
