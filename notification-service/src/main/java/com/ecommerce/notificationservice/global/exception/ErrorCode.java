package com.ecommerce.notificationservice.global.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {

    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "알림이 확인되지 않습니다."),
    NOTIFICATION_ACCESS_DENIED(HttpStatus.FORBIDDEN, "알림 접근 권한이 없습니다.")
    ;

    private final HttpStatus status;
    private final String message;
}
