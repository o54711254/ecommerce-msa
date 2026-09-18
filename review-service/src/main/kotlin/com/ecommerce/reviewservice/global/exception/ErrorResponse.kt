package com.ecommerce.reviewservice.global.exception

import org.springframework.http.ResponseEntity

class ErrorResponse(val code: String, val message: String) {

    /** companion object
     * - java의 static을 대신
     * - class 하나당 최대 한개
     */
    companion object {
        fun of(errorCode: ErrorCode): ResponseEntity<ErrorResponse> {
            return ResponseEntity.status(errorCode.status).body(ErrorResponse(errorCode.name, errorCode.message))
        }

        fun of(errorCode: ErrorCode, message: String): ResponseEntity<ErrorResponse> {
            return ResponseEntity.status(errorCode.status).body(ErrorResponse(errorCode.name, message))
        }
    }
}