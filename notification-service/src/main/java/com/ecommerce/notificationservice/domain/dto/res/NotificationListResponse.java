package com.ecommerce.notificationservice.domain.dto.res;

import com.ecommerce.notificationservice.domain.entity.NotificationType;

import java.time.LocalDateTime;

public record NotificationListResponse(
        Long id,
        NotificationType notificationType,
        Long orderId,
        Long paymentId,
        String content,
        boolean isRead,
        LocalDateTime createdAt
) {
}
