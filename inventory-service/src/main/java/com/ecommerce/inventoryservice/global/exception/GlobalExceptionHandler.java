package com.ecommerce.inventoryservice.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /* 비즈니스 로직 에러 */
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ErrorResponse> handle(BusinessException e) {
        log.error("{} - {}", e.getClass().getSimpleName(), e.getMessage());
        return ErrorResponse.of(e.getErrorCode());
    }

    /* 요청 값 검증 에러 */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    protected ResponseEntity<ErrorResponse> handle(MethodArgumentNotValidException e) {
        String message = e.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .findFirst()
                .orElse(ErrorCode.INVALID_INPUT.getMessage());
        log.warn("MethodArgumentNotValidException - {}", message);
        return ErrorResponse.of(ErrorCode.INVALID_INPUT, message);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    protected ResponseEntity<ErrorResponse> handle(MethodArgumentTypeMismatchException e) {
        log.warn("MethodArgumentTypeMismatchException - {}", e.getMessage());
        return ErrorResponse.of(ErrorCode.INVALID_INPUT);
    }

    /* 예상치 못한 서버 에러 */
    @ExceptionHandler(Exception.class)
    protected ResponseEntity<ErrorResponse> handle(Exception e) {
        log.error("Unexpected error", e);
        return ErrorResponse.of(ErrorCode.INTERNAL_SERVER_ERROR);
    }
}
