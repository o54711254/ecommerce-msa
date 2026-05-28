package com.ecommerce.orderservice.kafka.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;

@Configuration
public class KafkaConfig {

    @Bean
    public NewTopic orderFailedTopic() {
        return TopicBuilder.name("order.failed")
                .partitions(3)
                .build();
    }
}
