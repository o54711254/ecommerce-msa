package com.ecommerce.notificationservice.kafka.consumer;

import com.ecommerce.notificationservice.domain.dto.req.CreateNotificationRequest;
import com.ecommerce.notificationservice.domain.entity.NotificationType;
import com.ecommerce.notificationservice.domain.service.NotificationService;
import com.ecommerce.notificationservice.kafka.config.KafkaTopic;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentEventConsumerTest {

    @Mock private NotificationService notificationService;
    @Spy private ObjectMapper objectMapper = new ObjectMapper();
    @InjectMocks private PaymentEventConsumer paymentEventConsumer;

    @Nested
    @DisplayName("handlePaymentSuccess - payment.success 소비")
    class HandlePaymentSuccessTest {

        @Test
        void 성공_결제성공_알림생성() {
            String json = """
                    {"orderId":10,"memberId":1,"paymentId":100,"amount":5000}
                    """;

            paymentEventConsumer.handlePaymentSuccess(json);

            ArgumentCaptor<CreateNotificationRequest> captor = ArgumentCaptor.forClass(CreateNotificationRequest.class);
            verify(notificationService).createNotification(eq(KafkaTopic.PAYMENT_SUCCESS), captor.capture());
            assertThat(captor.getValue().getMemberId()).isEqualTo(1L);
            assertThat(captor.getValue().getOrderId()).isEqualTo(10L);
            assertThat(captor.getValue().getPaymentId()).isEqualTo(100L);
            assertThat(captor.getValue().getType()).isEqualTo(NotificationType.PAYMENT_SUCCESS);
        }

        @Test
        void 중복이벤트_DataIntegrityViolation_정상ACK() {
            String json = """
                    {"orderId":10,"memberId":1,"paymentId":100,"amount":5000}
                    """;
            willThrow(new DataIntegrityViolationException("duplicate"))
                    .given(notificationService).createNotification(any(), any());

            paymentEventConsumer.handlePaymentSuccess(json);

            verify(notificationService).createNotification(any(), any());
        }
    }

    @Nested
    @DisplayName("handlePaymentFailed - payment.failed 소비")
    class HandlePaymentFailedTest {

        @Test
        void 성공_결제실패_알림생성() {
            String json = """
                    {"orderId":10,"memberId":1}
                    """;

            paymentEventConsumer.handlePaymentFailed(json);

            ArgumentCaptor<CreateNotificationRequest> captor = ArgumentCaptor.forClass(CreateNotificationRequest.class);
            verify(notificationService).createNotification(eq(KafkaTopic.PAYMENT_FAILED), captor.capture());
            assertThat(captor.getValue().getMemberId()).isEqualTo(1L);
            assertThat(captor.getValue().getOrderId()).isEqualTo(10L);
            assertThat(captor.getValue().getType()).isEqualTo(NotificationType.PAYMENT_FAILED);
        }

        @Test
        void 중복이벤트_DataIntegrityViolation_정상ACK() {
            String json = """
                    {"orderId":10,"memberId":1}
                    """;
            willThrow(new DataIntegrityViolationException("duplicate"))
                    .given(notificationService).createNotification(any(), any());

            paymentEventConsumer.handlePaymentFailed(json);

            verify(notificationService).createNotification(any(), any());
        }
    }
}
