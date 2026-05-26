package com.ecommerce.memberservice.global.exception.custom;

import com.ecommerce.memberservice.global.exception.BusinessException;
import com.ecommerce.memberservice.global.exception.ErrorCode;

public class DuplicateEmailException extends BusinessException {

    public DuplicateEmailException() {
        super(ErrorCode.DUPLICATE_EMAIL);
    }
}
