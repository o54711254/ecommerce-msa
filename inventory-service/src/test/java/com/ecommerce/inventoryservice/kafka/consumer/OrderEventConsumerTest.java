package com.ecommerce.inventoryservice.kafka.consumer;

import com.ecommerce.inventoryservice.domain.dto.req.DecreaseProductInventoryRequest;
import com.ecommerce.inventoryservice.domain.service.InventoryService;
import com.ecommerce.inventoryservice.global.exception.BusinessException;
import com.ecommerce.inventoryservice.global.exception.ErrorCode;
import com.ecommerce.inventoryservice.kafka.config.KafkaTopic;
import com.ecommerce.inventoryservice.kafka.dto.InventoryDecreasedEvent;
import com.ecommerce.inventoryservice.kafka.dto.InventoryFailedEvent;
import com.ecommerce.inventoryservice.kafka.producer.InventoryEventProducer;
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
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderEventConsumerTest {

    @Mock private InventoryService inventoryService;
    @Mock private InventoryEventProducer inventoryEventProducer;
    @Spy private ObjectMapper objectMapper = new ObjectMapper();
    @InjectMocks private OrderEventConsumer orderEventConsumer;

    @Nested
    @DisplayName("handleOrderCreated - order.created 소비")
    class HandleOrderCreatedTest {

        @Test
        void 성공_재고차감_후_decreased_이벤트_발행() throws Exception {
            String json = """
                    {"memberId":1,"orderId":10,"amount":5000,"itemInfoList":[{"productId":1,"quantity":2}]}
                    """;
            given(inventoryService.decreaseProductInventoryIdempotent(
                    eq(KafkaTopic.ORDER_CREATED), eq(10L), any(DecreaseProductInventoryRequest.class)))
                    .willReturn(true);

            orderEventConsumer.handleOrderCreated(json);

            ArgumentCaptor<InventoryDecreasedEvent> captor = ArgumentCaptor.forClass(InventoryDecreasedEvent.class);
            verify(inventoryEventProducer).sendInventoryDecreased(captor.capture());
            assertThat(captor.getValue().orderId()).isEqualTo(10L);
            assertThat(captor.getValue().memberId()).isEqualTo(1L);
            assertThat(captor.getValue().amount()).isEqualTo(5000L);
        }

        @Test
        void 성공_이미처리된_이벤트_발행_생략() throws Exception {
            String json = """
                    {"memberId":1,"orderId":10,"amount":5000,"itemInfoList":[{"productId":1,"quantity":2}]}
                    """;
            given(inventoryService.decreaseProductInventoryIdempotent(
                    eq(KafkaTopic.ORDER_CREATED), eq(10L), any(DecreaseProductInventoryRequest.class)))
                    .willReturn(false);

            orderEventConsumer.handleOrderCreated(json);

            verify(inventoryEventProducer, never()).sendInventoryDecreased(any());
        }

        @Test
        void 실패_재고부족_failed_이벤트_발행() throws Exception {
            String json = """
                    {"memberId":1,"orderId":10,"amount":5000,"itemInfoList":[{"productId":1,"quantity":2}]}
                    """;
            given(inventoryService.decreaseProductInventoryIdempotent(
                    eq(KafkaTopic.ORDER_CREATED), eq(10L), any(DecreaseProductInventoryRequest.class)))
                    .willThrow(new BusinessException(ErrorCode.INSUFFICIENT_STOCK));

            orderEventConsumer.handleOrderCreated(json);

            ArgumentCaptor<InventoryFailedEvent> captor = ArgumentCaptor.forClass(InventoryFailedEvent.class);
            verify(inventoryEventProducer).sendInventoryFailed(captor.capture());
            assertThat(captor.getValue().orderId()).isEqualTo(10L);
        }

        @Test
        void 중복이벤트_DataIntegrityViolation_정상ACK() throws Exception {
            String json = """
                    {"memberId":1,"orderId":10,"amount":5000,"itemInfoList":[{"productId":1,"quantity":2}]}
                    """;
            given(inventoryService.decreaseProductInventoryIdempotent(
                    eq(KafkaTopic.ORDER_CREATED), eq(10L), any(DecreaseProductInventoryRequest.class)))
                    .willThrow(new DataIntegrityViolationException("duplicate"));

            orderEventConsumer.handleOrderCreated(json);

            verify(inventoryEventProducer, never()).sendInventoryDecreased(any());
            verify(inventoryEventProducer, never()).sendInventoryFailed(any());
        }

        @Test
        void 잘못된_JSON_RuntimeException_발생() {
            assertThatThrownBy(() -> orderEventConsumer.handleOrderCreated("invalid-json"))
                    .isInstanceOf(RuntimeException.class);
        }
    }

    @Nested
    @DisplayName("handleOrderFailed - order.failed 소비")
    class HandleOrderFailedTest {

        @Test
        void 성공_재고복구() {
            String json = """
                    {"orderId":10,"itemInfoList":[{"productId":1,"quantity":2}]}
                    """;

            orderEventConsumer.handleOrderFailed(json);

            verify(inventoryService).increaseProductInventoryIdempotent(
                    eq(KafkaTopic.ORDER_FAILED), eq(10L), any());
        }

        @Test
        void 중복이벤트_DataIntegrityViolation_정상ACK() {
            String json = """
                    {"orderId":10,"itemInfoList":[{"productId":1,"quantity":2}]}
                    """;
            willThrow(new DataIntegrityViolationException("duplicate"))
                    .given(inventoryService)
                    .increaseProductInventoryIdempotent(eq(KafkaTopic.ORDER_FAILED), eq(10L), any());

            orderEventConsumer.handleOrderFailed(json);

            verify(inventoryService).increaseProductInventoryIdempotent(any(), any(), any());
        }
    }

    @Nested
    @DisplayName("handleOrderCancelled - order.cancelled 소비")
    class HandleOrderCancelledTest {

        @Test
        void 성공_재고복구() {
            String json = """
                    {"memberId":1,"orderId":10,"itemInfoList":[{"productId":1,"quantity":2}]}
                    """;

            orderEventConsumer.handleOrderCancelled(json);

            verify(inventoryService).increaseProductInventoryIdempotent(
                    eq(KafkaTopic.ORDER_CANCELLED), eq(10L), any());
        }

        @Test
        void 중복이벤트_DataIntegrityViolation_정상ACK() {
            String json = """
                    {"memberId":1,"orderId":10,"itemInfoList":[{"productId":1,"quantity":2}]}
                    """;
            willThrow(new DataIntegrityViolationException("duplicate"))
                    .given(inventoryService)
                    .increaseProductInventoryIdempotent(eq(KafkaTopic.ORDER_CANCELLED), eq(10L), any());

            orderEventConsumer.handleOrderCancelled(json);

            verify(inventoryService).increaseProductInventoryIdempotent(any(), any(), any());
        }
    }
}
