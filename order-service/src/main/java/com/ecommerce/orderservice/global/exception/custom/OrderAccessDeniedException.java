package com.ecommerce.orderservice.global.exception.custom;

import com.ecommerce.orderservice.global.exception.BusinessException;
import com.ecommerce.orderservice.global.exception.ErrorCode;

public class OrderAccessDeniedException extends BusinessException {

    public OrderAccessDeniedException() {
        super(ErrorCode.ORDER_ACCESS_DENIED);
    }
}
