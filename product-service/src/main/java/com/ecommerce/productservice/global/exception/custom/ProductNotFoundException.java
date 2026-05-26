package com.ecommerce.productservice.global.exception.custom;

import com.ecommerce.productservice.global.exception.BusinessException;
import com.ecommerce.productservice.global.exception.ErrorCode;

public class ProductNotFoundException extends BusinessException {

    public ProductNotFoundException() {
        super(ErrorCode.PRODUCT_NOT_FOUND);
    }
}
