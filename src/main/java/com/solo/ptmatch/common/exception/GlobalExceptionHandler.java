package com.solo.ptmatch.common.exception;

import com.solo.ptmatch.common.response.ErrorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(GlobalException.class)
    public ResponseEntity<ErrorResponse> handleGlobalException(GlobalException exception) {
        ErrorCode errorCode = exception.getErrorCode();
        log.warn("GlobalException 발생: {}", errorCode.getMessage());

        return ResponseEntity.status(errorCode.getStatus()).body(ErrorResponse.from(exception));
    }

    // @Valid DTO 유효성 검증 실패 예외 처리
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        ErrorCode errorCode = ErrorCode.INVALID_REQUEST;
        String errorMessage = e.getBindingResult().getAllErrors().get(0).getDefaultMessage();
        log.warn("MethodArgumentNotValidException 발생: {}", errorMessage);

        return ResponseEntity.status(errorCode.getStatus()).body(ErrorResponse.of(errorCode, errorMessage));
    }

    // 나머지 모든 예외 공통 처리
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        ErrorCode errorCode = ErrorCode.INTERNAL_SERVER_ERROR;
        log.error("처리되지 않은 예외 발생: {}", e.getMessage(), e);
        
        return ResponseEntity.status(errorCode.getStatus()).body(ErrorResponse.of(errorCode, errorCode.getMessage()));
    }
}
