package com.ecommerce.reviewservice.global.exception.custom

import com.ecommerce.reviewservice.global.exception.BusinessException
import com.ecommerce.reviewservice.global.exception.ErrorCode

class InvalidRatingException : BusinessException(ErrorCode.INVALID_RATING)
