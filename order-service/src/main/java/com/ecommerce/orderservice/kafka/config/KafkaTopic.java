package com.ecommerce.orderservice.kafka.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum KafkaTopic {

    // Produce
    ORDER_CREATED(TopicName.ORDER_CREATED),
    ORDER_FAILED(TopicName.ORDER_FAILED),
    ORDER_CANCELLED(TopicName.ORDER_CANCELLED),

    // Consume
    INVENTORY_FAILED(TopicName.INVENTORY_FAILED),
    PAYMENT_SUCCESS(TopicName.PAYMENT_SUCCESS),
    PAYMENT_FAILED(TopicName.PAYMENT_FAILED),
    ;

    private final String topicName;

    public static final class TopicName {

        // Produce
        public static final String ORDER_CREATED = "order.created";
        public static final String ORDER_FAILED = "order.failed";
        public static final String ORDER_CANCELLED = "order.cancelled";

        // Consume
        public static final String INVENTORY_FAILED = "inventory.failed";
        public static final String PAYMENT_SUCCESS = "payment.success";
        public static final String PAYMENT_FAILED = "payment.failed";

        private TopicName() {
        }
    }
}
