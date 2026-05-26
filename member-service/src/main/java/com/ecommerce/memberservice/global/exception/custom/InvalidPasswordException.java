package com.ecommerce.memberservice.global.exception.custom;

import com.ecommerce.memberservice.global.exception.BusinessException;
import com.ecommerce.memberservice.global.exception.ErrorCode;

public class InvalidPasswordException extends BusinessException {

    public InvalidPasswordException() {
        super(ErrorCode.INVALID_PASSWORD);
    }
}
