package com.ecommerce.notificationservice.domain.service;

import com.ecommerce.notificationservice.domain.dto.req.CreateNotificationRequest;
import com.ecommerce.notificationservice.domain.entity.Notification;
import com.ecommerce.notificationservice.domain.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;

    @Transactional
    public void createNotification(CreateNotificationRequest request) {

        String content = String.format(request.getType().getMessage(), request.getOrderId());
        Notification notification = Notification.builder()
                .type(request.getType())
                .memberId(request.getMemberId())
                .orderId(request.getOrderId())
                .paymentId(request.getPaymentId())
                .content(content)
                .isRead(false)
                .build();

        notificationRepository.save(notification);
    }
}
