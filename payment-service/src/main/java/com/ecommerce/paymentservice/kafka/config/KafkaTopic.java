package com.ecommerce.paymentservice.kafka.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum KafkaTopic {

    // Produce
    PAYMENT_SUCCESS(TopicName.PAYMENT_SUCCESS),
    PAYMENT_FAILED(TopicName.PAYMENT_FAILED),

    // Consume
    INVENTORY_DECREASED(TopicName.INVENTORY_DECREASED);

    private final String topicName;

    public static final class TopicName {

        // Produce
        public static final String PAYMENT_SUCCESS = "payment.success";
        public static final String PAYMENT_FAILED = "payment.failed";

        // Consume
        public static final String INVENTORY_DECREASED = "inventory.decreased";

        private TopicName() {}
    }
}
