package com.ecommerce.inventoryservice.kafka.config;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum KafkaTopic {
    // Produce
    INVENTORY_DECREASED(TopicName.INVENTORY_DECREASED),
    INVENTORY_FAILED(TopicName.INVENTORY_FAILED),

    // Consume
    ORDER_CREATED(TopicName.ORDER_CREATED),
    ORDER_FAILED(TopicName.ORDER_FAILED),
    ORDER_CANCELLED(TopicName.ORDER_CANCELLED)
    ;

    private final String topicName;

    public static final class TopicName {
        // Produce
        public static final String INVENTORY_DECREASED = "inventory.decreased";
        public static final String INVENTORY_FAILED = "inventory.failed";

        // Consume
        public static final String ORDER_CREATED = "order.created";
        public static final String ORDER_FAILED = "order.failed";
        public static final String ORDER_CANCELLED = "order.cancelled";
        private TopicName() {}
    }
}
