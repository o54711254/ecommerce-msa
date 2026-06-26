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
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderEventConsumerTest {

    @Mock private NotificationService notificationService;
    @Spy private ObjectMapper objectMapper = new ObjectMapper();
    @InjectMocks private OrderEventConsumer orderEventConsumer;

    @Nested
    @DisplayName("handleOrderCancelled - order.cancelled 소비")
    class HandleOrderCancelledTest {

        @Test
        void 성공_주문취소_알림생성() {
            String json = """
                    {"memberId":1,"orderId":10,"itemInfoList":[{"productId":1,"quantity":2}]}
                    """;

            orderEventConsumer.handleOrderCancelled(json);

            ArgumentCaptor<CreateNotificationRequest> captor = ArgumentCaptor.forClass(CreateNotificationRequest.class);
            verify(notificationService).createNotification(eq(KafkaTopic.ORDER_CANCELLED), captor.capture());
            assertThat(captor.getValue().getMemberId()).isEqualTo(1L);
            assertThat(captor.getValue().getOrderId()).isEqualTo(10L);
            assertThat(captor.getValue().getType()).isEqualTo(NotificationType.ORDER_CANCELED);
        }

        @Test
        void 중복이벤트_DataIntegrityViolation_정상ACK() {
            String json = """
                    {"memberId":1,"orderId":10,"itemInfoList":[{"productId":1,"quantity":2}]}
                    """;
            willThrow(new DataIntegrityViolationException("duplicate"))
                    .given(notificationService).createNotification(any(), any());

            orderEventConsumer.handleOrderCancelled(json);

            verify(notificationService).createNotification(any(), any());
        }

        @Test
        void 잘못된_JSON_RuntimeException_발생() {
            assertThatThrownBy(() -> orderEventConsumer.handleOrderCancelled("invalid-json"))
                    .isInstanceOf(RuntimeException.class);
        }
    }
}
