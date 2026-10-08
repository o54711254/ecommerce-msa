package com.ecommerce.notificationservice.domain.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum NotificationType {

    PAYMENT_SUCCESS("결제 성공", "주문번호 [%d]에 대한 결제가 성공적으로 완료되었습니다.", ReferenceType.ORDER),
    PAYMENT_FAILED("결제 실패", "주문번호 [%d]에 대한 결제가 실패했습니다.", ReferenceType.ORDER),
    ORDER_CANCELED("주문 취소", "주문이 취소되었습니다. 주문번호[%d]", ReferenceType.ORDER),
    REVIEW_CREATED("리뷰 생성", "판매 상품에 대한 리뷰가 생성되었습니다.", ReferenceType.REVIEW);

    private final String description;
    private final String message;
    // 타입마다 참조 대상이 고정 → 외부에서 referenceType을 따로 받지 않아 불일치 방지
    private final ReferenceType referenceType;
}
