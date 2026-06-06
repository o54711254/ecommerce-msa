package com.ecommerce.notificationservice.global.exception.custom;

import com.ecommerce.notificationservice.global.exception.BusinessException;
import com.ecommerce.notificationservice.global.exception.ErrorCode;

public class NotificationNotFoundException extends BusinessException {

    public NotificationNotFoundException() {
        super(ErrorCode.NOTIFICATION_NOT_FOUND);
    }
}
