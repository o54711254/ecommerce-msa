package com.ecommerce.orderservice.domain.service;

import com.ecommerce.orderservice.client.payment.PaymentClient;
import com.ecommerce.orderservice.client.product.ProductClient;
import com.ecommerce.orderservice.client.product.dto.ProductNameResponse;
import com.ecommerce.orderservice.client.product.dto.ProductPriceResponse;
import com.ecommerce.orderservice.domain.dto.req.CreateOrderItemRequest;
import com.ecommerce.orderservice.domain.dto.req.CreateOrderRequest;
import com.ecommerce.orderservice.domain.dto.res.OrderListResponse;
import com.ecommerce.orderservice.domain.dto.res.OrderResponse;
import com.ecommerce.orderservice.domain.entity.Order;
import com.ecommerce.orderservice.domain.entity.OrderItem;
import com.ecommerce.orderservice.domain.entity.OrderStatus;
import com.ecommerce.orderservice.domain.repository.OrderRepository;
import com.ecommerce.orderservice.global.exception.custom.OrderAccessDeniedException;
import com.ecommerce.orderservice.global.exception.custom.OrderCancelNotAllowedException;
import com.ecommerce.orderservice.global.exception.custom.OrderNotFoundException;
import com.ecommerce.orderservice.kafka.config.KafkaTopic;
import com.ecommerce.orderservice.kafka.dto.OrderCancelEvent;
import com.ecommerce.orderservice.kafka.dto.OrderCreateEvent;
import com.ecommerce.orderservice.kafka.dto.OrderFailedEvent;
import com.ecommerce.orderservice.kafka.producer.OrderEventProducer;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private ProductClient productClient;
    @Mock private PaymentClient paymentClient;
    @Mock private OrderEventProducer orderEventProducer;
    @Mock private ProcessedEventService processedEventService;
    @InjectMocks private OrderService orderService;

    @Nested
    @DisplayName("getOrderList - 주문 목록 조회")
    class GetOrderListTest {

        @Test
        void memberId로_목록_반환() {
            Long memberId = 1L;
            List<OrderListResponse> expected = List.of(
                    new OrderListResponse(1L, OrderStatus.PENDING, 10000L, LocalDateTime.now())
            );
            given(orderRepository.getOrderList(memberId)).willReturn(expected);

            List<OrderListResponse> result = orderService.getOrderList(memberId);

            assertThat(result).isEqualTo(expected);
        }
    }

    @Nested
    @DisplayName("getOrderDetail - 주문 상세 조회")
    class GetOrderDetailTest {

        @Test
        void 성공() {
            Long memberId = 1L;
            Long orderId = 10L;
            OrderItem orderItem = OrderItem.create(100L, 2, 5000L);
            Order order = Order.create(memberId, List.of(orderItem));

            given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
            given(productClient.getNamesMap(anyList()))
                    .willReturn(new ProductNameResponse(Map.of(100L, "상품A")));

            OrderResponse response = orderService.getOrderDetail(memberId, orderId);

            assertThat(response.getOrderStatus()).isEqualTo(OrderStatus.PENDING);
            assertThat(response.getTotalPrice()).isEqualTo(10000L);
            assertThat(response.getItemList()).hasSize(1);

            var item = response.getItemList().get(0);
            assertThat(item.getProductName()).isEqualTo("상품A");
            assertThat(item.getQuantity()).isEqualTo(2);
            assertThat(item.getItemPrice()).isEqualTo(5000L);
            assertThat(item.getTotalPrice()).isEqualTo(10000L);
        }

        @Test
        void 실패_주문_없음() {
            given(orderRepository.findById(any())).willReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.getOrderDetail(1L, 999L))
                    .isInstanceOf(OrderNotFoundException.class);
        }

        @Test
        void 실패_본인_주문_아님() {
            Long memberId = 1L;
            Long otherMemberId = 2L;
            Order order = Order.create(memberId, List.of(OrderItem.create(100L, 1, 5000L)));

            given(orderRepository.findById(10L)).willReturn(Optional.of(order));

            assertThatThrownBy(() -> orderService.getOrderDetail(otherMemberId, 10L))
                    .isInstanceOf(OrderAccessDeniedException.class)
                    .hasMessage("본인 주문만 조회할 수 있습니다.");
        }
    }

    @Nested
    @DisplayName("createOrder - 주문 생성")
    class CreateOrderTest {

        @Test
        void 성공() {
            Long memberId = 1L;
            CreateOrderRequest request = new CreateOrderRequest(List.of(
                    new CreateOrderItemRequest(100L, 2),
                    new CreateOrderItemRequest(200L, 1)
            ));

            given(productClient.getPriceMap(List.of(100L, 200L)))
                    .willReturn(new ProductPriceResponse(Map.of(100L, 5000L, 200L, 3000L)));
            given(orderRepository.save(any(Order.class))).willAnswer(invocation -> {
                Order saved = invocation.getArgument(0);
                ReflectionTestUtils.setField(saved, "id", 1L);
                return saved;
            });

            Long orderId = orderService.createOrder(memberId, request);

            assertThat(orderId).isEqualTo(1L);
            verify(productClient).getPriceMap(List.of(100L, 200L));
            verify(orderRepository).save(any(Order.class));
            verify(orderEventProducer).sendOrderCreated(any(OrderCreateEvent.class));
        }

        @Test
        void order_created_이벤트에_올바른_상품정보가_담긴다() {
            Long memberId = 1L;
            CreateOrderRequest request = new CreateOrderRequest(List.of(
                    new CreateOrderItemRequest(100L, 3),
                    new CreateOrderItemRequest(200L, 2)
            ));

            given(productClient.getPriceMap(anyList()))
                    .willReturn(new ProductPriceResponse(Map.of(100L, 1000L, 200L, 2000L)));
            given(orderRepository.save(any(Order.class))).willAnswer(invocation -> {
                Order saved = invocation.getArgument(0);
                ReflectionTestUtils.setField(saved, "id", 1L);
                return saved;
            });

            orderService.createOrder(memberId, request);

            ArgumentCaptor<OrderCreateEvent> captor = ArgumentCaptor.forClass(OrderCreateEvent.class);
            verify(orderEventProducer).sendOrderCreated(captor.capture());

            OrderCreateEvent event = captor.getValue();
            assertThat(event.memberId()).isEqualTo(memberId);
            assertThat(event.amount()).isEqualTo(7000L); // 1000*3 + 2000*2
            assertThat(event.itemInfoList()).hasSize(2);
            assertThat(event.itemInfoList()).extracting("productId").containsExactlyInAnyOrder(100L, 200L);
            assertThat(event.itemInfoList()).extracting("quantity").containsExactlyInAnyOrder(3, 2);
        }
    }

    @Nested
    @DisplayName("cancelOrder - 주문 취소")
    class CancelOrderTest {

        @Test
        void 성공_PENDING_상태() {
            Long memberId = 1L;
            Long orderId = 10L;
            Order order = Order.create(memberId, List.of(OrderItem.create(100L, 2, 5000L)));
            ReflectionTestUtils.setField(order, "id", orderId);
            given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

            orderService.cancelOrder(memberId, orderId);

            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.CANCELED);
            verify(paymentClient).cancelPayment(orderId);
            verify(orderEventProducer).sendOrderCancelled(any(OrderCancelEvent.class));
        }

        @Test
        void 성공_PAID_상태() {
            Long memberId = 1L;
            Long orderId = 10L;
            Order order = Order.create(memberId, List.of(OrderItem.create(100L, 2, 5000L)));
            order.updateStatus(OrderStatus.PAID);
            ReflectionTestUtils.setField(order, "id", orderId);
            given(orderRepository.findById(orderId)).willReturn(Optional.of(order));

            orderService.cancelOrder(memberId, orderId);

            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.REFUNDED);
            verify(paymentClient).cancelPayment(orderId);
            verify(orderEventProducer).sendOrderCancelled(any(OrderCancelEvent.class));
        }

        @Test
        void 실패_주문_없음() {
            given(orderRepository.findById(any())).willReturn(Optional.empty());

            assertThatThrownBy(() -> orderService.cancelOrder(1L, 999L))
                    .isInstanceOf(OrderNotFoundException.class);
        }

        @Test
        void 실패_본인_주문_아님() {
            Order order = Order.create(1L, List.of(OrderItem.create(100L, 1, 5000L)));
            given(orderRepository.findById(10L)).willReturn(Optional.of(order));

            assertThatThrownBy(() -> orderService.cancelOrder(2L, 10L))
                    .isInstanceOf(OrderAccessDeniedException.class);
        }

        @Test
        void 실패_취소_불가_상태() {
            Order order = Order.create(1L, List.of(OrderItem.create(100L, 1, 5000L)));
            order.updateStatus(OrderStatus.FAILED);
            given(orderRepository.findById(10L)).willReturn(Optional.of(order));

            assertThatThrownBy(() -> orderService.cancelOrder(1L, 10L))
                    .isInstanceOf(OrderCancelNotAllowedException.class);
        }
    }

    @Nested
    @DisplayName("updateOrderStatus - 주문 상태 변경")
    class UpdateOrderStatusTest {

        @Test
        void 성공() {
            Long orderId = 10L;
            Order order = Order.create(1L, List.of(OrderItem.create(100L, 1, 5000L)));
            given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
            given(processedEventService.saveOrSkipOrderEvent(any(), any())).willReturn(true);

            orderService.updateOrderStatus(KafkaTopic.PAYMENT_SUCCESS, orderId, OrderStatus.PAID);

            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PAID);
        }

        @Test
        void 멱등성_중복_호출시_스킵() {
            Long orderId = 10L;
            given(processedEventService.saveOrSkipOrderEvent(any(), any())).willReturn(false);

            orderService.updateOrderStatus(KafkaTopic.PAYMENT_SUCCESS, orderId, OrderStatus.PAID);

            verify(orderRepository, never()).findById(any());
        }
    }

    @Nested
    @DisplayName("handlePaymentFailed - 결제 실패 처리")
    class HandlePaymentFailedTest {

        @Test
        void 성공_주문_FAILED_상태_변경_및_이벤트_발행() {
            Long orderId = 10L;
            Order order = Order.create(1L, List.of(
                    OrderItem.create(100L, 2, 5000L),
                    OrderItem.create(200L, 1, 3000L)
            ));
            given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
            given(processedEventService.saveOrSkipOrderEvent(any(), any())).willReturn(true);

            orderService.handlePaymentFailed(KafkaTopic.PAYMENT_FAILED, orderId);

            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.FAILED);

            ArgumentCaptor<OrderFailedEvent> captor = ArgumentCaptor.forClass(OrderFailedEvent.class);
            verify(orderEventProducer).sendOrderFailed(captor.capture());
            assertThat(captor.getValue().orderId()).isEqualTo(orderId);
            assertThat(captor.getValue().itemInfoList()).hasSize(2);
        }

        @Test
        void 멱등성_중복_호출시_스킵() {
            Long orderId = 10L;
            given(processedEventService.saveOrSkipOrderEvent(any(), any())).willReturn(false);

            orderService.handlePaymentFailed(KafkaTopic.PAYMENT_FAILED, orderId);

            verify(orderRepository, never()).findById(any());
            verify(orderEventProducer, never()).sendOrderFailed(any());
        }
    }
}
