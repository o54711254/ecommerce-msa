package com.ecommerce.reviewservice.global.exception.custom

import com.ecommerce.reviewservice.global.exception.BusinessException
import com.ecommerce.reviewservice.global.exception.ErrorCode

class OrderNotPaidException : BusinessException(ErrorCode.ORDER_NOT_PAID)