package com.ecommerce.productservice.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "product")
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long sellerId;

    @Column(nullable = false)
    private String name;

    @Lob
    private String description;

    @Column(nullable = false)
    private Long price;

    @Enumerated(EnumType.STRING)
    @Column(columnDefinition = "varchar(20)")
    private ProductStatus status;

    private Product(Long sellerId, String name, String description, Long price) {
        this.sellerId = sellerId;
        this.name = name;
        this.description = description;
        this.price = price;
        this.status = ProductStatus.AVAILABLE;
    }

    public void update(String name, String description, Long price) {
        this.name = name;
        this.description = description;
        this.price = price;
    }

    public static Product create(Long sellerId, String name, String description, Long price) {
        return new Product(sellerId, name, description, price);
    }
}
