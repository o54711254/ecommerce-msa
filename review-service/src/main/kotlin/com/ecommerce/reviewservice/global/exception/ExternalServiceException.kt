package com.ecommerce.reviewservice.global.exception

class ExternalServiceException(serviceName: String, cause: Throwable?) :
    RuntimeException("$serviceName 서비스를 사용할 수 없습니다.", cause)
