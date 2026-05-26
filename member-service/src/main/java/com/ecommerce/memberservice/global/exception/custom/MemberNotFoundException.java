package com.ecommerce.memberservice.global.exception.custom;

import com.ecommerce.memberservice.global.exception.BusinessException;
import com.ecommerce.memberservice.global.exception.ErrorCode;

public class MemberNotFoundException extends BusinessException {

    public MemberNotFoundException() {
        super(ErrorCode.MEMBER_NOT_FOUND);
    }
}
