package com.ecommerce.inventoryservice.global.exception.custom;

import com.ecommerce.inventoryservice.global.exception.BusinessException;
import com.ecommerce.inventoryservice.global.exception.ErrorCode;

public class InsufficientStockException extends BusinessException {

    public InsufficientStockException() {
        super(ErrorCode.INSUFFICIENT_STOCK);
    }
}
