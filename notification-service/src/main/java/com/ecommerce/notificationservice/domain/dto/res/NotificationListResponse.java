package com.ecommerce.notificationservice.domain.dto.res;

import com.ecommerce.notificationservice.domain.entity.NotificationType;
import com.ecommerce.notificationservice.domain.entity.ReferenceType;

import java.time.LocalDateTime;

public record NotificationListResponse(
        Long id,
        NotificationType notificationType,
        ReferenceType referenceType,
        Long referenceId,
        String content,
        boolean isRead,
        LocalDateTime createdAt
) {
}
