package com.ecommerce.notificationservice.domain.service;

import com.ecommerce.notificationservice.domain.dto.req.CreateNotificationRequest;
import com.ecommerce.notificationservice.domain.dto.res.NotificationListResponse;
import com.ecommerce.notificationservice.domain.entity.Notification;
import com.ecommerce.notificationservice.domain.entity.NotificationType;
import com.ecommerce.notificationservice.domain.repository.NotificationRepository;
import com.ecommerce.notificationservice.global.exception.custom.NotificationAccessDeniedException;
import com.ecommerce.notificationservice.global.exception.custom.NotificationNotFoundException;
import com.ecommerce.notificationservice.kafka.config.KafkaTopic;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Service
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final ProcessedEventService processedEventService;

    @Transactional
    public void createNotification(KafkaTopic kafkaTopic, CreateNotificationRequest request) {
        if (!processedEventService.saveOrSkipEvent(kafkaTopic, request.getReferenceId())) {
            return;
        }
        notificationRepository.save(Notification.create(request.getMemberId(), request.getType(), request.getReferenceId()));
    }

    @Transactional(readOnly = true)
    public Page<NotificationListResponse> getNotificationList(Long memberId, NotificationType notificationType, Pageable pageable) {
        return notificationRepository.getNotificationPage(memberId, notificationType, pageable);
    }

    @Transactional
    public void readNotification(Long memberId, Long id) {
        Notification notification = notificationRepository.findById(id).orElseThrow(NotificationNotFoundException::new);
        if (!notification.getMemberId().equals(memberId)) {
            throw new NotificationAccessDeniedException();
        }
        notification.read();
    }

    @Transactional
    public void deleteNotification(Long memberId, Long id) {
        Notification notification = notificationRepository.findById(id).orElseThrow(NotificationNotFoundException::new);
        if (!notification.getMemberId().equals(memberId)) {
            throw new NotificationAccessDeniedException();
        }
        notificationRepository.delete(notification);
    }
}
