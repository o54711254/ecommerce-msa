package com.ecommerce.notificationservice.domain.dto.req;

import com.ecommerce.notificationservice.domain.entity.NotificationType;
import com.ecommerce.notificationservice.kafka.dto.OrderCancelEvent;
import com.ecommerce.notificationservice.kafka.dto.PaymentFailedEvent;
import com.ecommerce.notificationservice.kafka.dto.PaymentSuccessEvent;
import com.ecommerce.notificationservice.kafka.dto.ReviewCreatedEvent;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CreateNotificationRequest {
    private Long memberId;
    private NotificationType type;
    // type의 ReferenceType에 해당하는 대상 ID (ORDER → orderId, REVIEW → reviewId)
    private Long referenceId;

    public CreateNotificationRequest(PaymentSuccessEvent event) {
        this(event.memberId(), NotificationType.PAYMENT_SUCCESS, event.orderId());
    }

    public CreateNotificationRequest(PaymentFailedEvent event) {
        this(event.memberId(), NotificationType.PAYMENT_FAILED, event.orderId());
    }

    public CreateNotificationRequest(OrderCancelEvent event) {
        this(event.memberId(), NotificationType.ORDER_CANCELED, event.orderId());
    }

    public CreateNotificationRequest(Long memberId, ReviewCreatedEvent event) {
        this(memberId, NotificationType.REVIEW_CREATED, event.reviewId());
    }

    private CreateNotificationRequest(Long memberId, NotificationType type, Long referenceId) {
        this.memberId = memberId;
        this.type = type;
        this.referenceId = referenceId;
    }
}
