package com.ecommerce.notificationservice.global.exception.custom;

import com.ecommerce.notificationservice.global.exception.BusinessException;
import com.ecommerce.notificationservice.global.exception.ErrorCode;

public class NotificationAccessDeniedException extends BusinessException {

    public NotificationAccessDeniedException() {
        super(ErrorCode.NOTIFICATION_ACCESS_DENIED);
    }
}
