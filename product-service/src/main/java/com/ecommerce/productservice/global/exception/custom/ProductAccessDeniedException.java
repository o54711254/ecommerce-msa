package com.ecommerce.productservice.global.exception.custom;

import com.ecommerce.productservice.global.exception.BusinessException;
import com.ecommerce.productservice.global.exception.ErrorCode;

public class ProductAccessDeniedException extends BusinessException {

    public ProductAccessDeniedException() {
        super(ErrorCode.PRODUCT_ACCESS_DENIED);
    }
}
