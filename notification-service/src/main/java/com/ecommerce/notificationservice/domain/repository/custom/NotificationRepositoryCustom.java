package com.ecommerce.notificationservice.domain.repository.custom;

import com.ecommerce.notificationservice.domain.dto.res.NotificationListResponse;
import com.ecommerce.notificationservice.domain.entity.NotificationType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationRepositoryCustom {

    Page<NotificationListResponse> getNotificationPage(Long memberId, NotificationType notificationType, Pageable pageable);
}
