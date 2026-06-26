package com.ecommerce.paymentservice.kafka.consumer;

import com.ecommerce.paymentservice.domain.dto.req.CreatePaymentRequest;
import com.ecommerce.paymentservice.domain.service.PaymentService;
import com.ecommerce.paymentservice.kafka.config.KafkaTopic;
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

    @Mock private PaymentService paymentService;
    @Spy private ObjectMapper objectMapper = new ObjectMapper();
    @InjectMocks private OrderEventConsumer orderEventConsumer;

    @Nested
    @DisplayName("handleInventoryDecreased - inventory.decreased 소비")
    class HandleInventoryDecreasedTest {

        @Test
        void 성공_결제생성() {
            String json = """
                    {"orderId":10,"memberId":1,"amount":5000}
                    """;

            orderEventConsumer.handleInventoryDecreased(json);

            ArgumentCaptor<CreatePaymentRequest> captor = ArgumentCaptor.forClass(CreatePaymentRequest.class);
            verify(paymentService).createPayment(eq(KafkaTopic.INVENTORY_DECREASED), eq(1L), captor.capture());
            assertThat(captor.getValue().orderId()).isEqualTo(10L);
            assertThat(captor.getValue().amount()).isEqualTo(5000L);
        }

        @Test
        void 중복이벤트_DataIntegrityViolation_정상ACK() {
            String json = """
                    {"orderId":10,"memberId":1,"amount":5000}
                    """;
            willThrow(new DataIntegrityViolationException("duplicate"))
                    .given(paymentService).createPayment(any(), any(), any());

            orderEventConsumer.handleInventoryDecreased(json);

            verify(paymentService).createPayment(any(), any(), any());
        }

        @Test
        void 잘못된_JSON_RuntimeException_발생() {
            assertThatThrownBy(() -> orderEventConsumer.handleInventoryDecreased("invalid-json"))
                    .isInstanceOf(RuntimeException.class);
        }
    }
}
