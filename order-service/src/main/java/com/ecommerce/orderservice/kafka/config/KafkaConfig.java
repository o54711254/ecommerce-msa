package com.ecommerce.orderservice.kafka.config;

import com.fasterxml.jackson.core.JsonParseException;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.common.config.TopicConfig;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.TopicBuilder;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

import java.time.Duration;

@Configuration
public class KafkaConfig {

    // 메시지 삭제주기. 비즈니스 로직에 따라 달라지겠지만 30일로 설정
    private static final String DELETE_PERIOD = String.valueOf(Duration.ofDays(30).toMillis());

    @Bean
    public NewTopic orderCreatedTopic() {
        return TopicBuilder.name("order.created")
                .partitions(3)
                // 단일 브로커 환경이라 1 고정, 운영에서는 브로커 수만큼 늘려야 함
                .replicas(1)
                .config(TopicConfig.RETENTION_MS_CONFIG, DELETE_PERIOD)
                .build();
    }

    @Bean
    public NewTopic orderFailedTopic() {
        return TopicBuilder.name("order.failed")
                .partitions(3)
                .replicas(1)
                .config(TopicConfig.RETENTION_MS_CONFIG, DELETE_PERIOD)
                .build();
    }

    @Bean
    public NewTopic orderCancelledTopic() {
        return TopicBuilder.name("order.cancelled")
                .partitions(3)
                .replicas(1)
                .config(TopicConfig.RETENTION_MS_CONFIG, DELETE_PERIOD)
                .build();
    }

    @Bean
    public DefaultErrorHandler errorHandler(KafkaTemplate<String, String> kafkaTemplate) {

        // 실패한 메시지를 보낼 DLT 발행자
        DeadLetterPublishingRecoverer deadLetterPublishingRecoverer = new DeadLetterPublishingRecoverer(kafkaTemplate);

        // 재시도, 안되면 DLT로 보냄
        FixedBackOff backOff = new FixedBackOff(1000L, 3);
        DefaultErrorHandler errorHandler = new DefaultErrorHandler(deadLetterPublishingRecoverer, backOff);

        // 재시도 해도 의미 없는 에러는 바로 넘김
        errorHandler.addNotRetryableExceptions(
                JsonParseException.class
        );
        return errorHandler;
    }
}
