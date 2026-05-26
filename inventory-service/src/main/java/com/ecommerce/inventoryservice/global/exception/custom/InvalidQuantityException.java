package com.ecommerce.inventoryservice.global.exception.custom;

import com.ecommerce.inventoryservice.global.exception.BusinessException;
import com.ecommerce.inventoryservice.global.exception.ErrorCode;

public class InvalidQuantityException extends BusinessException {

    public InvalidQuantityException() {
        super(ErrorCode.INVALID_QUANTITY);
    }
}
