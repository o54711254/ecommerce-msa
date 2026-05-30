package com.ecommerce.orderservice.global.exception.custom;

import com.ecommerce.orderservice.global.exception.BusinessException;
import com.ecommerce.orderservice.global.exception.ErrorCode;

public class OrderCancelNotAllowedException extends BusinessException {

    public OrderCancelNotAllowedException() {
        super(ErrorCode.ORDER_CANCEL_NOT_ALLOWED);
    }
}
