package com.ecommerce.paymentservice.global.exception.custom;

import com.ecommerce.paymentservice.global.exception.BusinessException;
import com.ecommerce.paymentservice.global.exception.ErrorCode;

public class PaymentCancelNotAllowedException extends BusinessException {

    public PaymentCancelNotAllowedException() {
        super(ErrorCode.PAYMENT_CANCEL_NOT_ALLOWED);
    }
}
