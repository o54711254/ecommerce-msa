package com.ecommerce.inventoryservice.global.exception.custom;

import com.ecommerce.inventoryservice.global.exception.BusinessException;
import com.ecommerce.inventoryservice.global.exception.ErrorCode;

public class InventoryNotFoundException extends BusinessException {

    public InventoryNotFoundException() {
        super(ErrorCode.INVENTORY_NOT_FOUND);
    }
}
