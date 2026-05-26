package com.ecommerce.orderservice.global.exception.custom;

import com.ecommerce.orderservice.global.exception.BusinessException;
import com.ecommerce.orderservice.global.exception.ErrorCode;

public class OrderNotFoundException extends BusinessException {

    public OrderNotFoundException() {
        super(ErrorCode.ORDER_NOT_FOUND);
    }
}
