package com.ecommerce.orderservice.domain.entity;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OrderTest {

    @Nested
    @DisplayName("create - 주문 생성")
    class CreateTest {

        @Test
        void totalPrice는_아이템_가격의_합() {
            OrderItem item1 = OrderItem.create(1L, 2, 5000L); // 10000
            OrderItem item2 = OrderItem.create(2L, 3, 2000L); // 6000

            Order order = Order.create(1L, List.of(item1, item2));

            assertThat(order.getTotalPrice()).isEqualTo(16000L);
        }

        @Test
        void 초기_상태는_PENDING() {
            Order order = Order.create(1L, List.of(OrderItem.create(1L, 1, 1000L)));

            assertThat(order.getOrderStatus()).isEqualTo(OrderStatus.PENDING);
        }

        @Test
        void OrderItem에_Order가_연결된다() {
            OrderItem item = OrderItem.create(1L, 1, 1000L);

            Order order = Order.create(1L, List.of(item));

            assertThat(item.getOrder()).isSameAs(order);
        }
    }
}
