package com.ecommerce.notificationservice.global.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    /* 비즈니스 로직 에러 */
    @ExceptionHandler(BusinessException.class)
    protected ResponseEntity<ErrorResponse> handle(BusinessException e) {
        log.error("{} - {}", e.getClass().getSimpleName(), e.getMessage());
        return ErrorResponse.of(e.getErrorCode());
    }

}
