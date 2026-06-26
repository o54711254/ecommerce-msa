package com.ecommerce.paymentservice.kafka.producer;

import com.ecommerce.paymentservice.kafka.config.KafkaTopic;
import com.ecommerce.paymentservice.kafka.dto.PaymentFailedEvent;
import com.ecommerce.paymentservice.kafka.dto.PaymentSuccessEvent;
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
import org.springframework.kafka.core.KafkaTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class PaymentEventProducerTest {

    @Mock private KafkaTemplate<String, String> kafkaTemplate;
    @Spy private ObjectMapper objectMapper = new ObjectMapper();
    @InjectMocks private PaymentEventProducer paymentEventProducer;

    @Nested
    @DisplayName("sendPaymentSuccess - payment.success 발행")
    class SendPaymentSuccessTest {

        @Test
        void 올바른_토픽_키_JSON으로_발행() throws Exception {
            PaymentSuccessEvent event = new PaymentSuccessEvent(10L, 1L, 100L, 5000L);

            paymentEventProducer.sendPaymentSuccess(event);

            ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
            verify(kafkaTemplate).send(eq(KafkaTopic.TopicName.PAYMENT_SUCCESS), eq("10"), jsonCaptor.capture());

            PaymentSuccessEvent captured = objectMapper.readValue(jsonCaptor.getValue(), PaymentSuccessEvent.class);
            assertThat(captured.orderId()).isEqualTo(10L);
            assertThat(captured.memberId()).isEqualTo(1L);
            assertThat(captured.paymentId()).isEqualTo(100L);
            assertThat(captured.amount()).isEqualTo(5000L);
        }
    }

    @Nested
    @DisplayName("sendPaymentFailed - payment.failed 발행")
    class SendPaymentFailedTest {

        @Test
        void 올바른_토픽_키_JSON으로_발행() throws Exception {
            PaymentFailedEvent event = new PaymentFailedEvent(10L, 1L);

            paymentEventProducer.sendPaymentFailed(event);

            ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
            verify(kafkaTemplate).send(eq(KafkaTopic.TopicName.PAYMENT_FAILED), eq("10"), jsonCaptor.capture());

            PaymentFailedEvent captured = objectMapper.readValue(jsonCaptor.getValue(), PaymentFailedEvent.class);
            assertThat(captured.orderId()).isEqualTo(10L);
            assertThat(captured.memberId()).isEqualTo(1L);
        }
    }
}
