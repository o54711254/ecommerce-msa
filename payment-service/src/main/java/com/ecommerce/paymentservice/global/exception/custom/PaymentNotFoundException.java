package com.ecommerce.paymentservice.global.exception.custom;

import com.ecommerce.paymentservice.global.exception.BusinessException;
import com.ecommerce.paymentservice.global.exception.ErrorCode;

public class PaymentNotFoundException extends BusinessException {

    public PaymentNotFoundException() {
        super(ErrorCode.PAYMENT_NOT_FOUND);
    }
}
