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
        if (!processedEventService.saveOrSkipEvent(kafkaTopic, request.getTargetId())) {
            return;
        }
        // 리뷰 알림은 message에 %d 없음 → orderId(null) 넘겨도 String.format이 초과 인자 무시
        String content = String.format(request.getType().getMessage(), request.getOrderId());
        Notification notification = Notification.builder()
                .type(request.getType())
                .memberId(request.getMemberId())
                .orderId(request.getOrderId())
                .paymentId(request.getPaymentId())
                .reviewId(request.getReviewId())
                .content(content)
                .isRead(false)
                .build();
        notificationRepository.save(notification);
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
