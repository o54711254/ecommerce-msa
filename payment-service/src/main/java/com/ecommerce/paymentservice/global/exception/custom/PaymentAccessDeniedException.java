package com.ecommerce.paymentservice.global.exception.custom;

import com.ecommerce.paymentservice.global.exception.BusinessException;
import com.ecommerce.paymentservice.global.exception.ErrorCode;

public class PaymentAccessDeniedException extends BusinessException {

    public PaymentAccessDeniedException() {
        super(ErrorCode.PAYMENT_ACCESS_DENIED);
    }
}
