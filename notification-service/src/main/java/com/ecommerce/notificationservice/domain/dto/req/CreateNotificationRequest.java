package com.ecommerce.notificationservice.domain.dto.req;

import com.ecommerce.notificationservice.domain.entity.NotificationType;
import com.ecommerce.notificationservice.kafka.dto.OrderCancelEvent;
import com.ecommerce.notificationservice.kafka.dto.PaymentFailedEvent;
import com.ecommerce.notificationservice.kafka.dto.PaymentSuccessEvent;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CreateNotificationRequest {
    private Long memberId;
    private NotificationType type;
    private Long orderId;
    private Long paymentId;

    // 결제 성공엔 반드시 주문과 결제 ID가 있어야함
    public CreateNotificationRequest(PaymentSuccessEvent event) {
        this.memberId = event.memberId();
        this.type = NotificationType.PAYMENT_SUCCESS;
        this.orderId = event.orderId();
        this.paymentId = event.paymentId();
    }

    // 결제 실패는 주문번호만 들어감
    public CreateNotificationRequest(PaymentFailedEvent event) {
        this.memberId = event.memberId();
        this.type = NotificationType.PAYMENT_FAILED;
        this.orderId = event.orderId();
    }

    // 주문 취소도 주문번호만 들어감
    public CreateNotificationRequest(OrderCancelEvent event) {
        this.memberId = event.memberId();
        this.type = NotificationType.ORDER_CANCELED;
        this.orderId = event.orderId();
    }
}
