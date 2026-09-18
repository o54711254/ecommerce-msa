package com.ecommerce.reviewservice.global.exception

abstract class BusinessException(val errorCode: ErrorCode) : RuntimeException(errorCode.message)