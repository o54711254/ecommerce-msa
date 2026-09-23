package com.ecommerce.reviewservice.global.exception

import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice

@RestControllerAdvice
class GlobalExceptionHandler {

    // Slf4j는 Lombok이고 Kotlin은 없으므로 직접 선언
    private val log = LoggerFactory.getLogger(javaClass)

    @ExceptionHandler(BusinessException::class)
    fun handle(e: BusinessException): ResponseEntity<ErrorResponse> {
        log.warn("{} - {}", e.javaClass.simpleName, e.message)
        return ErrorResponse.of(e.errorCode)
    }

    @ExceptionHandler(ExternalServiceException::class)
    fun handle(e: ExternalServiceException): ResponseEntity<ErrorResponse> {
        log.error("{} - {}", e.javaClass.simpleName, e.message)
        return ErrorResponse.of(ErrorCode.EXTERNAL_SERVICE_UNAVAILABLE)
    }
}
