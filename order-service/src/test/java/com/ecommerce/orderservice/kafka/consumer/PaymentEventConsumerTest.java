package com.ecommerce.orderservice.kafka.consumer;

import com.ecommerce.orderservice.domain.entity.OrderStatus;
import com.ecommerce.orderservice.domain.service.OrderService;
import com.ecommerce.orderservice.kafka.config.KafkaTopic;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentEventConsumerTest {

    @Mock private OrderService orderService;
    @Spy private ObjectMapper objectMapper = new ObjectMapper();
    @InjectMocks private PaymentEventConsumer paymentEventConsumer;

    @Nested
    @DisplayName("handlePaymentSuccess - payment.success 소비")
    class HandlePaymentSuccessTest {

        @Test
        void 성공_주문상태_PAID로_변경() {
            String json = """
                    {"orderId":10,"memberId":1,"paymentId":100,"amount":5000}
                    """;

            paymentEventConsumer.handlePaymentSuccess(json);

            verify(orderService).updateOrderStatus(KafkaTopic.PAYMENT_SUCCESS, 10L, OrderStatus.PAID);
        }

        @Test
        void 중복이벤트_DataIntegrityViolation_정상ACK() {
            String json = """
                    {"orderId":10,"memberId":1,"paymentId":100,"amount":5000}
                    """;
            willThrow(new DataIntegrityViolationException("duplicate"))
                    .given(orderService).updateOrderStatus(eq(KafkaTopic.PAYMENT_SUCCESS), eq(10L), eq(OrderStatus.PAID));

            paymentEventConsumer.handlePaymentSuccess(json);

            verify(orderService).updateOrderStatus(any(), any(), any());
        }

        @Test
        void 잘못된_JSON_RuntimeException_발생() {
            assertThatThrownBy(() -> paymentEventConsumer.handlePaymentSuccess("invalid-json"))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("handlePaymentFailed - payment.failed 소비")
    class HandlePaymentFailedTest {

        @Test
        void 성공_결제실패_처리() {
            String json = """
                    {"orderId":10,"memberId":1}
                    """;

            paymentEventConsumer.handlePaymentFailed(json);

            verify(orderService).handlePaymentFailed(KafkaTopic.PAYMENT_FAILED, 10L);
        }

        @Test
        void 중복이벤트_DataIntegrityViolation_정상ACK() {
            String json = """
                    {"orderId":10,"memberId":1}
                    """;
            willThrow(new DataIntegrityViolationException("duplicate"))
                    .given(orderService).handlePaymentFailed(eq(KafkaTopic.PAYMENT_FAILED), eq(10L));

            paymentEventConsumer.handlePaymentFailed(json);

            verify(orderService).handlePaymentFailed(any(), any());
        }
    }
}
