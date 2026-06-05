package com.ecommerce.orderservice.domain.repository;

import com.ecommerce.orderservice.AbstractIntegrationTest;
import com.ecommerce.orderservice.domain.dto.res.OrderListResponse;
import com.ecommerce.orderservice.domain.entity.Order;
import com.ecommerce.orderservice.domain.entity.OrderItem;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderRepositoryIntegrationTest extends AbstractIntegrationTest {

    @Autowired private OrderRepository orderRepository;
    @Autowired private OrderItemRepository orderItemRepository;

    @Nested
    @Transactional
    @DisplayName("getOrderList - 주문 목록 조회")
    class GetOrderListTest {

        @BeforeEach
        void setUp() {
            orderItemRepository.deleteAll();
            orderItemRepository.flush();
            orderRepository.deleteAll();
            orderRepository.flush();
        }

        @Test
        void 본인_주문만_반환() {
            orderRepository.saveAll(List.of(
                    Order.create(1L, List.of(OrderItem.create(100L, 1, 5000L))),
                    Order.create(1L, List.of(OrderItem.create(200L, 2, 3000L))),
                    Order.create(2L, List.of(OrderItem.create(300L, 1, 8000L)))
            ));

            List<OrderListResponse> result = orderRepository.getOrderList(1L);

            assertThat(result).hasSize(2);
        }

        @Test
        void createdAt_역순_정렬() throws InterruptedException {
            Order first = orderRepository.saveAndFlush(Order.create(1L, List.of(OrderItem.create(100L, 1, 5000L))));
            Thread.sleep(10);
            Order second = orderRepository.saveAndFlush(Order.create(1L, List.of(OrderItem.create(200L, 1, 3000L))));

            List<OrderListResponse> result = orderRepository.getOrderList(1L);

            assertThat(result).hasSize(2);
            assertThat(result.get(0).orderId()).isEqualTo(second.getId());
            assertThat(result.get(1).orderId()).isEqualTo(first.getId());
        }

        @Test
        void 주문_없으면_빈_리스트() {
            List<OrderListResponse> result = orderRepository.getOrderList(1L);

            assertThat(result).isEmpty();
        }
    }
}
