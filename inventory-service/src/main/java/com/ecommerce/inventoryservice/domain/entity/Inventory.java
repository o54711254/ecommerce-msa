package com.ecommerce.inventoryservice.domain.entity;

import com.ecommerce.inventoryservice.global.exception.custom.InsufficientStockException;
import com.ecommerce.inventoryservice.global.exception.custom.InvalidQuantityException;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "inventory")
public class Inventory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private Long productId;

    @Column(nullable = false)
    private Integer quantity;

    private Inventory(Long productId, Integer quantity) {
        this.productId = productId;
        this.quantity = quantity;
    }

    public static Inventory create(Long productId, Integer quantity) {
        return new Inventory(productId, quantity);
    }

    public void addQuantity(int quantity) {
        if (quantity <= 0) throw new InvalidQuantityException();
        this.quantity += quantity;
    }

    public void decreaseQuantity(int quantity) {
        if (this.quantity < quantity) {
            throw new InsufficientStockException();
        }
        this.quantity -= quantity;
    }
}
