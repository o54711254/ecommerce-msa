package com.ecommerce.orderservice.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "orders")
public class Order extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private OrderStatus orderStatus;

    @Column(nullable = false)
    private Long totalPrice;

    @OneToMany(mappedBy = "order", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderItem> orderItems;

    private Order(Long memberId, Long totalPrice, List<OrderItem> orderItems) {
        this.memberId = memberId;
        this.orderStatus = OrderStatus.PENDING;
        this.totalPrice = totalPrice;
        this.orderItems = orderItems;
        orderItems.forEach(item -> item.assignOrder(this));
    }

    public static Order create(Long memberId, List<OrderItem> orderItems) {
        Long totalPrice = orderItems.stream()
                .mapToLong(item -> item.getItemPrice() * item.getQuantity())
                .sum();
        return new Order(memberId, totalPrice, orderItems);
    }
}
