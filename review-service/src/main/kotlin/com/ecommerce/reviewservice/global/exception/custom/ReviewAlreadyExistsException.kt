package com.ecommerce.reviewservice.global.exception.custom

import com.ecommerce.reviewservice.global.exception.BusinessException
import com.ecommerce.reviewservice.global.exception.ErrorCode

class ReviewAlreadyExistsException : BusinessException(ErrorCode.REVIEW_ALREADY_EXISTS)