package com.ecommerce.orderservice.domain.service;

import com.ecommerce.orderservice.AbstractIntegrationTest;
import com.ecommerce.orderservice.client.inventory.InventoryClient;
import com.ecommerce.orderservice.client.payment.PaymentClient;
import com.ecommerce.orderservice.client.product.ProductClient;
import com.ecommerce.orderservice.client.product.dto.ProductPriceResponse;
import com.ecommerce.orderservice.domain.dto.req.CreateOrderItemRequest;
import com.ecommerce.orderservice.domain.dto.req.CreateOrderRequest;
import com.ecommerce.orderservice.domain.entity.Order;
import com.ecommerce.orderservice.domain.entity.OrderItem;
import com.ecommerce.orderservice.domain.entity.OrderStatus;
import com.ecommerce.orderservice.domain.repository.OrderItemRepository;
import com.ecommerce.orderservice.domain.repository.OrderRepository;
import com.ecommerce.orderservice.kafka.dto.OrderCancelEvent;
import com.ecommerce.orderservice.kafka.dto.OrderFailedEvent;
import com.ecommerce.orderservice.kafka.producer.OrderEventProducer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

class OrderServiceIntegrationTest extends AbstractIntegrationTest {

    @Autowired private OrderService orderService;
    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderItemRepository orderItemRepository;

    @MockitoBean private ProductClient productClient;
    @MockitoBean private InventoryClient inventoryClient;
    @MockitoBean private PaymentClient paymentClient;
    @MockitoBean private OrderEventProducer orderEventProducer;

    @BeforeEach
    void setUp() {
        orderItemRepository.deleteAll();
        orderRepository.deleteAll();
    }

    @Nested
    @DisplayName("createOrder - 주문 생성")
    class CreateOrderTest {

        @Test
        void 성공_주문과_아이템이_DB에_저장() {
            Long memberId = 1L;
            CreateOrderRequest request = new CreateOrderRequest(List.of(
                    new CreateOrderItemRequest(100L, 2),
                    new CreateOrderItemRequest(200L, 1)
            ));
            given(productClient.getPriceMap(anyList()))
                    .willReturn(ResponseEntity.ok(new ProductPriceResponse(Map.of(100L, 5000L, 200L, 3000L))));

            Long orderId = orderService.createOrder(memberId, request);

            Order saved = orderRepository.findById(orderId).orElseThrow();
            assertThat(saved.getMemberId()).isEqualTo(memberId);
            assertThat(saved.getOrderStatus()).isEqualTo(OrderStatus.PENDING);
            assertThat(saved.getTotalPrice()).isEqualTo(13000L); // 5000*2 + 3000*1
            assertThat(orderItemRepository.count()).isEqualTo(2);
        }

    }

    @Nested
    @DisplayName("cancelOrder - 주문 취소")
    class CancelOrderTest {

        @Test
        void 성공_PENDING_CANCELED로_변경() {
            Order order = orderRepository.save(Order.create(1L, List.of(OrderItem.create(100L, 2, 5000L))));

            orderService.cancelOrder(1L, order.getId());

            Order updated = orderRepository.findById(order.getId()).orElseThrow();
            assertThat(updated.getOrderStatus()).isEqualTo(OrderStatus.CANCELED);
            verify(orderEventProducer).sendOrderCancelled(ArgumentCaptor.forClass(OrderCancelEvent.class).capture());
        }

        @Test
        void 성공_PAID_REFUNDED로_변경() {
            Order order = orderRepository.save(Order.create(1L, List.of(OrderItem.create(100L, 2, 5000L))));
            orderService.updateOrderStatus(order.getId(), OrderStatus.PAID);

            orderService.cancelOrder(1L, order.getId());

            Order updated = orderRepository.findById(order.getId()).orElseThrow();
            assertThat(updated.getOrderStatus()).isEqualTo(OrderStatus.REFUNDED);
        }
    }

    @Nested
    @DisplayName("updateOrderStatus - 주문 상태 변경")
    class UpdateOrderStatusTest {

        @Test
        void 성공_상태_변경() {
            Order order = orderRepository.save(Order.create(1L, List.of(OrderItem.create(100L, 1, 5000L))));

            orderService.updateOrderStatus(order.getId(), OrderStatus.PAID);

            Order updated = orderRepository.findById(order.getId()).orElseThrow();
            assertThat(updated.getOrderStatus()).isEqualTo(OrderStatus.PAID);
        }
    }

    @Nested
    @DisplayName("orderFailed - 주문 실패 처리")
    class OrderFailedTest {

        @Test
        void 성공_Kafka_이벤트_발행() {
            Order order = orderRepository.save(Order.create(1L, List.of(
                    OrderItem.create(100L, 2, 5000L),
                    OrderItem.create(200L, 1, 3000L)
            )));

            orderService.orderFailed(order.getId());

            ArgumentCaptor<OrderFailedEvent> captor = ArgumentCaptor.forClass(OrderFailedEvent.class);
            verify(orderEventProducer).sendOrderFailed(captor.capture());
            assertThat(captor.getValue().orderId()).isEqualTo(order.getId());
            assertThat(captor.getValue().itemInfoList()).hasSize(2);
        }
    }
}
