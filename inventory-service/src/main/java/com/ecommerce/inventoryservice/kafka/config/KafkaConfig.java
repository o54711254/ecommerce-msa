package com.ecommerce.inventoryservice.kafka.config;

import com.fasterxml.jackson.core.JsonParseException;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.listener.DeadLetterPublishingRecoverer;
import org.springframework.kafka.listener.DefaultErrorHandler;
import org.springframework.util.backoff.FixedBackOff;

@Configuration
public class KafkaConfig {

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
