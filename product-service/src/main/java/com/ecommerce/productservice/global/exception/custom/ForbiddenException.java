package com.ecommerce.productservice.global.exception.custom;

import com.ecommerce.productservice.global.exception.BusinessException;
import com.ecommerce.productservice.global.exception.ErrorCode;

public class ForbiddenException extends BusinessException {

    public ForbiddenException() {
        super(ErrorCode.FORBIDDEN);
    }
}
