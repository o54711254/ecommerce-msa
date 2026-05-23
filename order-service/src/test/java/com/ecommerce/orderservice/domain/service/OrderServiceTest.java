package com.ecommerce.orderservice.domain.service;

import com.ecommerce.orderservice.client.inventory.InventoryClient;
import com.ecommerce.orderservice.client.inventory.dto.DecreaseProductInventoryRequest;
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
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
import static org.mockito.Mockito.verify;

import com.ecommerce.orderservice.client.inventory.dto.OrderInfoRequest;
import org.mockito.ArgumentCaptor;

@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

    @Mock private OrderRepository orderRepository;
    @Mock private ProductClient productClient;
    @Mock private InventoryClient inventoryClient;
    @InjectMocks private OrderService orderService;

    @Test
    void getOrderList_memberId로_목록_반환() {
        Long memberId = 1L;
        List<OrderListResponse> expected = List.of(
                new OrderListResponse(1L, OrderStatus.PENDING, 10000L, LocalDateTime.now())
        );
        given(orderRepository.getOrderList(memberId)).willReturn(expected);

        List<OrderListResponse> result = orderService.getOrderList(memberId);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    void getOrderDetail_정상_조회() {
        Long memberId = 1L;
        Long orderId = 10L;
        OrderItem orderItem = OrderItem.create(100L, 2, 5000L);
        Order order = Order.create(memberId, List.of(orderItem));

        given(orderRepository.findById(orderId)).willReturn(Optional.of(order));
        given(productClient.getNamesMap(anyList()))
                .willReturn(ResponseEntity.ok(new ProductNameResponse(Map.of(100L, "상품A"))));

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
    void getOrderDetail_주문이_없으면_EntityNotFoundException() {
        given(orderRepository.findById(any())).willReturn(Optional.empty());

        assertThatThrownBy(() -> orderService.getOrderDetail(1L, 999L))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    void getOrderDetail_본인_주문이_아니면_IllegalStateException() {
        Long memberId = 1L;
        Long otherMemberId = 2L;
        Order order = Order.create(memberId, List.of(OrderItem.create(100L, 1, 5000L)));

        given(orderRepository.findById(10L)).willReturn(Optional.of(order));

        assertThatThrownBy(() -> orderService.getOrderDetail(otherMemberId, 10L))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("본인 주문만 조회할 수 있습니다");
    }

    @Test
    void createOrder_정상_생성() {
        Long memberId = 1L;
        CreateOrderRequest request = new CreateOrderRequest(List.of(
                new CreateOrderItemRequest(100L, 2),
                new CreateOrderItemRequest(200L, 1)
        ));

        given(productClient.getPriceMap(List.of(100L, 200L)))
                .willReturn(ResponseEntity.ok(new ProductPriceResponse(Map.of(100L, 5000L, 200L, 3000L))));
        given(inventoryClient.decreaseInventory(any(DecreaseProductInventoryRequest.class)))
                .willReturn(ResponseEntity.ok().build());

        Order savedOrder = Order.create(memberId, List.of(
                OrderItem.create(100L, 2, 5000L),
                OrderItem.create(200L, 1, 3000L)
        ));
        ReflectionTestUtils.setField(savedOrder, "id", 1L);
        given(orderRepository.save(any(Order.class))).willReturn(savedOrder);

        Long orderId = orderService.createOrder(memberId, request);

        assertThat(orderId).isEqualTo(1L);
        verify(productClient).getPriceMap(List.of(100L, 200L));
        verify(inventoryClient).decreaseInventory(any(DecreaseProductInventoryRequest.class));
        verify(orderRepository).save(any(Order.class));
    }

    @Test
    void createOrder_재고차감_요청에_올바른_상품정보가_담긴다() {
        Long memberId = 1L;
        CreateOrderRequest request = new CreateOrderRequest(List.of(
                new CreateOrderItemRequest(100L, 3),
                new CreateOrderItemRequest(200L, 2)
        ));

        given(productClient.getPriceMap(anyList()))
                .willReturn(ResponseEntity.ok(new ProductPriceResponse(Map.of(100L, 1000L, 200L, 2000L))));
        given(inventoryClient.decreaseInventory(any(DecreaseProductInventoryRequest.class)))
                .willReturn(ResponseEntity.ok().build());
        given(orderRepository.save(any(Order.class))).willAnswer(invocation -> {
            Order saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 1L);
            return saved;
        });

        orderService.createOrder(memberId, request);

        ArgumentCaptor<DecreaseProductInventoryRequest> captor = ArgumentCaptor.forClass(DecreaseProductInventoryRequest.class);
        verify(inventoryClient).decreaseInventory(captor.capture());

        List<OrderInfoRequest> captured = captor.getValue().orderInfoRequest();
        assertThat(captured).hasSize(2);
        assertThat(captured).extracting(OrderInfoRequest::productId).containsExactlyInAnyOrder(100L, 200L);
        assertThat(captured).extracting(OrderInfoRequest::quantity).containsExactlyInAnyOrder(3, 2);
    }
}
