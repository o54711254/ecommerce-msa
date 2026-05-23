package com.ecommerce.orderservice.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "order_item")
public class OrderItem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "order_id")
    private Order order;

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private int quantity;

    @Column(name = "item_price", nullable = false)
    private Long itemPrice;

    private OrderItem(Long productId, int quantity, Long itemPrice) {
        this.productId = productId;
        this.quantity = quantity;
        this.itemPrice = itemPrice;
    }

    public static OrderItem create(Long productId, int quantity, Long itemPrice) {
        return new OrderItem(productId, quantity, itemPrice);
    }

    void assignOrder(Order order) {
        this.order = order;
    }
}
