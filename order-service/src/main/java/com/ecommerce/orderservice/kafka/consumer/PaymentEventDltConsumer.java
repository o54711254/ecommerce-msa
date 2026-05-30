package com.ecommerce.orderservice.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class PaymentEventDltConsumer {

    // payment.success, payment.failed 두 토픽의 DLT를 하나의 리스너에서 처리
    @KafkaListener(topics = {"payment.success.DLT", "payment.failed.DLT"}, groupId = "orderGroup-dlt")
    public void handle(String rawJson,
                       @Header(KafkaHeaders.DLT_ORIGINAL_TOPIC) String originalTopic,
                       @Header(KafkaHeaders.DLT_EXCEPTION_MESSAGE) String exceptionMessage) {

        log.error("DLT 메시지 수신 - originalTopic: {}, cause: {}, payload: {}", originalTopic, exceptionMessage, rawJson);
        // 슬랙 알림 등 추가
    }
}
