package com.ecommerce.paymentservice.domain.entity;

import lombok.Getter;

@Getter
public enum PaymentStatus {

    PENDING,
    SUCCESS,
    FAILED,
    REFUNDED,
    CANCELLED
}
