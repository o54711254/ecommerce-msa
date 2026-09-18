package com.ecommerce.reviewservice.global.exception.custom

import com.ecommerce.reviewservice.global.exception.BusinessException
import com.ecommerce.reviewservice.global.exception.ErrorCode

class ReviewNotFoundException : BusinessException(ErrorCode.REVIEW_NOT_FOUND)
