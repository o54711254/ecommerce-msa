package com.ecommerce.reviewservice.global.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(val status: HttpStatus, val message: String) {
    REVIEW_ALREADY_EXISTS(HttpStatus.CONFLICT, "이미 작성된 리뷰가 있습니다."),
    ORDER_NOT_PAID(HttpStatus.BAD_REQUEST, "결제 완료된 주문만 리뷰 작성이 가능합니다."),
    PRODUCT_NOT_IN_ORDER(HttpStatus.BAD_REQUEST, "주문에 해당 상품이 포함되어 있지 않습니다."),
    REVIEW_NOT_FOUND(HttpStatus.NOT_FOUND, "리뷰를 찾을 수 없습니다."),
    REVIEW_ACCESS_DENIED(HttpStatus.FORBIDDEN, "본인의 리뷰만 수정/삭제할 수 있습니다."),
    INVALID_RATING(HttpStatus.BAD_REQUEST, "별점은 1~5 사이여야 합니다."),
}
