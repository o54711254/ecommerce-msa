package com.ecommerce.reviewservice.global.exception.custom

import com.ecommerce.reviewservice.global.exception.BusinessException
import com.ecommerce.reviewservice.global.exception.ErrorCode

class ReviewAccessDeniedException : BusinessException(ErrorCode.REVIEW_ACCESS_DENIED)
