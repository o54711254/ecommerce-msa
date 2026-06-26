package com.ecommerce.orderservice.kafka.producer;

import com.ecommerce.orderservice.kafka.config.KafkaTopic;
import com.ecommerce.orderservice.kafka.dto.OrderCancelEvent;
import com.ecommerce.orderservice.kafka.dto.OrderCreateEvent;
import com.ecommerce.orderservice.kafka.dto.OrderFailedEvent;
import com.ecommerce.orderservice.kafka.dto.OrderItemInfo;
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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderEventProducerTest {

    @Mock private KafkaTemplate<String, String> kafkaTemplate;
    @Spy private ObjectMapper objectMapper = new ObjectMapper();
    @InjectMocks private OrderEventProducer orderEventProducer;

    @Nested
    @DisplayName("sendOrderCreated - order.created 발행")
    class SendOrderCreatedTest {

        @Test
        void 올바른_토픽_키_JSON으로_발행() throws Exception {
            OrderCreateEvent event = new OrderCreateEvent(1L, 10L, 5000L,
                    List.of(new OrderItemInfo(1L, 2)));

            orderEventProducer.sendOrderCreated(event);

            ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
            verify(kafkaTemplate).send(eq(KafkaTopic.TopicName.ORDER_CREATED), eq("10"), jsonCaptor.capture());

            OrderCreateEvent captured = objectMapper.readValue(jsonCaptor.getValue(), OrderCreateEvent.class);
            assertThat(captured.orderId()).isEqualTo(10L);
            assertThat(captured.memberId()).isEqualTo(1L);
            assertThat(captured.amount()).isEqualTo(5000L);
            assertThat(captured.itemInfoList()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("sendOrderFailed - order.failed 발행")
    class SendOrderFailedTest {

        @Test
        void 올바른_토픽_키_JSON으로_발행() throws Exception {
            OrderFailedEvent event = new OrderFailedEvent(10L, List.of(new OrderItemInfo(1L, 2)));

            orderEventProducer.sendOrderFailed(event);

            ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
            verify(kafkaTemplate).send(eq(KafkaTopic.TopicName.ORDER_FAILED), eq("10"), jsonCaptor.capture());

            OrderFailedEvent captured = objectMapper.readValue(jsonCaptor.getValue(), OrderFailedEvent.class);
            assertThat(captured.orderId()).isEqualTo(10L);
            assertThat(captured.itemInfoList()).hasSize(1);
        }
    }

    @Nested
    @DisplayName("sendOrderCancelled - order.cancelled 발행")
    class SendOrderCancelledTest {

        @Test
        void 올바른_토픽_키_JSON으로_발행() throws Exception {
            OrderCancelEvent event = new OrderCancelEvent(1L, 10L, List.of(new OrderItemInfo(1L, 2)));

            orderEventProducer.sendOrderCancelled(event);

            ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
            verify(kafkaTemplate).send(eq(KafkaTopic.TopicName.ORDER_CANCELLED), eq("10"), jsonCaptor.capture());

            OrderCancelEvent captured = objectMapper.readValue(jsonCaptor.getValue(), OrderCancelEvent.class);
            assertThat(captured.orderId()).isEqualTo(10L);
            assertThat(captured.memberId()).isEqualTo(1L);
        }
    }
}
