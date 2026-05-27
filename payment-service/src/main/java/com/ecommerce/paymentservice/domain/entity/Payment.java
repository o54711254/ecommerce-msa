package com.ecommerce.paymentservice.domain.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@Table(name = "payment")
@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private Long memberId;

    @Column(nullable = false)
    private Long orderId;

    @Column(nullable = false)
    private Long amount;

    @Enumerated(EnumType.STRING)
    private PaymentStatus paymentStatus;

    private Payment(Long memberId, Long orderId, Long amount) {
        this.memberId = memberId;
        this.orderId = orderId;
        this.amount = amount;
        this.paymentStatus = PaymentStatus.PENDING;
    }

    public static Payment create(Long memberId, Long orderId, Long amount){
        return new Payment(memberId, orderId, amount);
    }
}
