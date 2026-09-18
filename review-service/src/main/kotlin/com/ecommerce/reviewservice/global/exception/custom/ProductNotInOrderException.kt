package com.ecommerce.reviewservice.global.exception.custom

import com.ecommerce.reviewservice.global.exception.BusinessException
import com.ecommerce.reviewservice.global.exception.ErrorCode

class ProductNotInOrderException : BusinessException(ErrorCode.PRODUCT_NOT_IN_ORDER)
