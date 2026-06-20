package com.ecommerce.notificationservice.kafka.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum KafkaTopic {

    // Consume
    PAYMENT_SUCCESS(TopicName.PAYMENT_SUCCESS),
    PAYMENT_FAILED(TopicName.PAYMENT_FAILED),
    ORDER_CANCELLED(TopicName.ORDER_CANCELLED);

    private final String topicName;

    public static final class TopicName {

        // Consume
        public static final String PAYMENT_SUCCESS = "payment.success";
        public static final String PAYMENT_FAILED = "payment.failed";
        public static final String ORDER_CANCELLED = "order.cancelled";

        private TopicName() {}
    }
}
