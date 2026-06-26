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
class InventoryEventConsumerTest {

    @Mock private OrderService orderService;
    @Spy private ObjectMapper objectMapper = new ObjectMapper();
    @InjectMocks private InventoryEventConsumer inventoryEventConsumer;

    @Nested
    @DisplayName("handleInventoryFailed - inventory.failed 소비")
    class HandleInventoryFailedTest {

        @Test
        void 성공_주문상태_FAILED로_변경() {
            String json = """
                    {"orderId":10}
                    """;

            inventoryEventConsumer.handleInventoryFailed(json);

            verify(orderService).updateOrderStatus(KafkaTopic.INVENTORY_FAILED, 10L, OrderStatus.FAILED);
        }

        @Test
        void 중복이벤트_DataIntegrityViolation_정상ACK() {
            String json = """
                    {"orderId":10}
                    """;
            willThrow(new DataIntegrityViolationException("duplicate"))
                    .given(orderService).updateOrderStatus(eq(KafkaTopic.INVENTORY_FAILED), eq(10L), eq(OrderStatus.FAILED));

            inventoryEventConsumer.handleInventoryFailed(json);

            verify(orderService).updateOrderStatus(any(), any(), any());
        }

        @Test
        void 잘못된_JSON_RuntimeException_발생() {
            assertThatThrownBy(() -> inventoryEventConsumer.handleInventoryFailed("invalid-json"))
                    .isInstanceOf(RuntimeException.class);
        }
    }
}
