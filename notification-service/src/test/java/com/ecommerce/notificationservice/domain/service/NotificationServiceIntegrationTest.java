package com.ecommerce.notificationservice.domain.service;

import com.ecommerce.notificationservice.AbstractIntegrationTest;
import com.ecommerce.notificationservice.domain.dto.res.NotificationListResponse;
import com.ecommerce.notificationservice.domain.entity.Notification;
import com.ecommerce.notificationservice.domain.entity.NotificationType;
import com.ecommerce.notificationservice.domain.repository.NotificationRepository;
import com.ecommerce.notificationservice.global.exception.custom.NotificationAccessDeniedException;
import com.ecommerce.notificationservice.global.exception.custom.NotificationNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class NotificationServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired private NotificationService notificationService;
    @Autowired private NotificationRepository notificationRepository;

    @BeforeEach
    void setUp() {
        notificationRepository.deleteAll();
    }

    Notification saveNotification(Long memberId, NotificationType type) {
        return notificationRepository.save(Notification.builder()
                .memberId(memberId)
                .type(type)
                .orderId(10L)
                .content("테스트 알림")
                .isRead(false)
                .build());
    }

    @Nested
    @DisplayName("getNotificationList - 알림 목록 조회")
    class GetNotificationListTest {

        @Test
        void 성공_전체_조회() {
            saveNotification(1L, NotificationType.PAYMENT_SUCCESS);
            saveNotification(1L, NotificationType.PAYMENT_FAILED);
            saveNotification(2L, NotificationType.PAYMENT_SUCCESS);

            Page<NotificationListResponse> result = notificationService.getNotificationList(1L, null, PageRequest.of(0, 10));

            assertThat(result.getTotalElements()).isEqualTo(2);
        }

        @Test
        void 성공_타입_필터_조회() {
            saveNotification(1L, NotificationType.PAYMENT_SUCCESS);
            saveNotification(1L, NotificationType.PAYMENT_FAILED);

            Page<NotificationListResponse> result = notificationService.getNotificationList(
                    1L, NotificationType.PAYMENT_SUCCESS, PageRequest.of(0, 10));

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).notificationType()).isEqualTo(NotificationType.PAYMENT_SUCCESS);
        }

        @Test
        void 성공_isRead_포함_반환() {
            saveNotification(1L, NotificationType.PAYMENT_SUCCESS);

            Page<NotificationListResponse> result = notificationService.getNotificationList(1L, null, PageRequest.of(0, 10));

            assertThat(result.getContent().get(0).isRead()).isFalse();
        }
    }

    @Nested
    @DisplayName("readNotification - 알림 읽음 처리")
    class ReadNotificationTest {

        @Test
        void 성공_isRead_true로_변경() {
            Notification notification = saveNotification(1L, NotificationType.PAYMENT_SUCCESS);

            notificationService.readNotification(1L, notification.getId());

            Notification updated = notificationRepository.findById(notification.getId()).orElseThrow();
            assertThat(updated.isRead()).isTrue();
        }

        @Test
        void 실패_알림_없음() {
            assertThatThrownBy(() -> notificationService.readNotification(1L, 999L))
                    .isInstanceOf(NotificationNotFoundException.class);
        }

        @Test
        void 실패_본인_알림_아님() {
            Notification notification = saveNotification(1L, NotificationType.PAYMENT_SUCCESS);

            assertThatThrownBy(() -> notificationService.readNotification(2L, notification.getId()))
                    .isInstanceOf(NotificationAccessDeniedException.class);
        }
    }

    @Nested
    @DisplayName("deleteNotification - 알림 삭제")
    class DeleteNotificationTest {

        @Test
        void 성공_DB에서_삭제() {
            Notification notification = saveNotification(1L, NotificationType.PAYMENT_SUCCESS);

            notificationService.deleteNotification(1L, notification.getId());

            assertThat(notificationRepository.findById(notification.getId())).isEmpty();
        }

        @Test
        void 실패_알림_없음() {
            assertThatThrownBy(() -> notificationService.deleteNotification(1L, 999L))
                    .isInstanceOf(NotificationNotFoundException.class);
        }

        @Test
        void 실패_본인_알림_아님() {
            Notification notification = saveNotification(1L, NotificationType.PAYMENT_SUCCESS);

            assertThatThrownBy(() -> notificationService.deleteNotification(2L, notification.getId()))
                    .isInstanceOf(NotificationAccessDeniedException.class);
        }
    }
}
