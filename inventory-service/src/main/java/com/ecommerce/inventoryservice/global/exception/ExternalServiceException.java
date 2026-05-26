package com.ecommerce.inventoryservice.global.exception;

public class ExternalServiceException extends RuntimeException {

    public ExternalServiceException(String serviceName, Throwable cause) {
        super(serviceName + " 서비스를 사용할 수 없습니다.", cause);
    }
}
