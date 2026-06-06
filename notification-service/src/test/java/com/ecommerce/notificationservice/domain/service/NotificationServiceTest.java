package com.ecommerce.notificationservice.domain.service;

import com.ecommerce.notificationservice.domain.dto.req.CreateNotificationRequest;
import com.ecommerce.notificationservice.domain.dto.res.NotificationListResponse;
import com.ecommerce.notificationservice.domain.entity.Notification;
import com.ecommerce.notificationservice.domain.entity.NotificationType;
import com.ecommerce.notificationservice.domain.repository.NotificationRepository;
import com.ecommerce.notificationservice.global.exception.custom.NotificationAccessDeniedException;
import com.ecommerce.notificationservice.global.exception.custom.NotificationNotFoundException;
import com.ecommerce.notificationservice.kafka.dto.OrderCancelEvent;
import com.ecommerce.notificationservice.kafka.dto.PaymentFailedEvent;
import com.ecommerce.notificationservice.kafka.dto.PaymentSuccessEvent;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock private NotificationRepository notificationRepository;
    @InjectMocks private NotificationService notificationService;

    @Nested
    @DisplayName("createNotification - 알림 생성")
    class CreateNotificationTest {

        @Test
        void 성공_결제완료_알림() {
            CreateNotificationRequest request = new CreateNotificationRequest(new PaymentSuccessEvent(10L, 1L, 5L, 50000L));
            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);

            notificationService.createNotification(request);

            verify(notificationRepository).save(captor.capture());
            Notification saved = captor.getValue();
            assertThat(saved.getMemberId()).isEqualTo(1L);
            assertThat(saved.getType()).isEqualTo(NotificationType.PAYMENT_SUCCESS);
            assertThat(saved.getOrderId()).isEqualTo(10L);
            assertThat(saved.getPaymentId()).isEqualTo(5L);
            assertThat(saved.isRead()).isFalse();
        }

        @Test
        void 성공_결제실패_알림() {
            CreateNotificationRequest request = new CreateNotificationRequest(new PaymentFailedEvent(10L, 1L));

            notificationService.createNotification(request);

            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            verify(notificationRepository).save(captor.capture());
            assertThat(captor.getValue().getType()).isEqualTo(NotificationType.PAYMENT_FAILED);
        }

        @Test
        void 성공_주문취소_알림() {
            CreateNotificationRequest request = new CreateNotificationRequest(new OrderCancelEvent(1L, 10L, List.of()));

            notificationService.createNotification(request);

            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
            verify(notificationRepository).save(captor.capture());
            assertThat(captor.getValue().getType()).isEqualTo(NotificationType.ORDER_CANCELED);
        }
    }

    @Nested
    @DisplayName("getNotificationList - 알림 목록 조회")
    class GetNotificationListTest {

        @Test
        void 성공() {
            PageRequest pageable = PageRequest.of(0, 10);
            Page<NotificationListResponse> page = new PageImpl<>(List.of());
            given(notificationRepository.getNotificationPage(1L, null, pageable)).willReturn(page);

            Page<NotificationListResponse> result = notificationService.getNotificationList(1L, null, pageable);

            assertThat(result).isNotNull();
            verify(notificationRepository).getNotificationPage(1L, null, pageable);
        }
    }

    @Nested
    @DisplayName("readNotification - 알림 읽음 처리")
    class ReadNotificationTest {

        @Test
        void 성공() {
            Notification notification = Notification.builder()
                    .memberId(1L).type(NotificationType.PAYMENT_SUCCESS)
                    .orderId(10L).isRead(false).build();
            given(notificationRepository.findById(1L)).willReturn(Optional.of(notification));

            notificationService.readNotification(1L, 1L);

            assertThat(notification.isRead()).isTrue();
        }

        @Test
        void 실패_알림_없음() {
            given(notificationRepository.findById(any())).willReturn(Optional.empty());

            assertThatThrownBy(() -> notificationService.readNotification(1L, 999L))
                    .isInstanceOf(NotificationNotFoundException.class);
        }

        @Test
        void 실패_본인_알림_아님() {
            Notification notification = Notification.builder()
                    .memberId(1L).type(NotificationType.PAYMENT_SUCCESS)
                    .orderId(10L).isRead(false).build();
            given(notificationRepository.findById(1L)).willReturn(Optional.of(notification));

            assertThatThrownBy(() -> notificationService.readNotification(2L, 1L))
                    .isInstanceOf(NotificationAccessDeniedException.class);
        }
    }

    @Nested
    @DisplayName("deleteNotification - 알림 삭제")
    class DeleteNotificationTest {

        @Test
        void 성공() {
            Notification notification = Notification.builder()
                    .memberId(1L).type(NotificationType.PAYMENT_SUCCESS)
                    .orderId(10L).isRead(false).build();
            ReflectionTestUtils.setField(notification, "id", 1L);
            given(notificationRepository.findById(1L)).willReturn(Optional.of(notification));

            notificationService.deleteNotification(1L, 1L);

            verify(notificationRepository).delete(notification);
        }

        @Test
        void 실패_알림_없음() {
            given(notificationRepository.findById(any())).willReturn(Optional.empty());

            assertThatThrownBy(() -> notificationService.deleteNotification(1L, 999L))
                    .isInstanceOf(NotificationNotFoundException.class);
        }

        @Test
        void 실패_본인_알림_아님() {
            Notification notification = Notification.builder()
                    .memberId(1L).type(NotificationType.PAYMENT_SUCCESS)
                    .orderId(10L).isRead(false).build();
            given(notificationRepository.findById(1L)).willReturn(Optional.of(notification));

            assertThatThrownBy(() -> notificationService.deleteNotification(2L, 1L))
                    .isInstanceOf(NotificationAccessDeniedException.class);
        }
    }
}
