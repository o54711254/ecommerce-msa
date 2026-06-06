package com.ecommerce.notificationservice.domain.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum NotificationType {

    PAYMENT_SUCCESS("결제 성공", "주문번호 [%d]에 대한 결제가 성공적으로 완료되었습니다."),
    PAYMENT_FAILED("결제 실패", "주문번호 [%d]에 대한 결제가 실패했습니다."),
    ORDER_CANCELED("주문 취소", "주문이 취소되었습니다. 주문번호[%d]")

    ;

    private final String description;
    private final String message;
}
