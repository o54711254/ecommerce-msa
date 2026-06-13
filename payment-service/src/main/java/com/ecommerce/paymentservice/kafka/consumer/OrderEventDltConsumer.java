package com.ecommerce.paymentservice.kafka.consumer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.KafkaHeaders;
import org.springframework.messaging.handler.annotation.Header;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderEventDltConsumer {

    @KafkaListener(topics = "inventory.decreased-dlt", groupId = "paymentGroup-dlt")
    public void handle(String rawJson, @Header(KafkaHeaders.DLT_ORIGINAL_TOPIC) String originalTopic, @Header(KafkaHeaders.DLT_EXCEPTION_MESSAGE) String exceptionMessage) {

        log.error("DLT 메시지 수신 - originalTopic: {}, cause: {}, payload: {}", originalTopic, exceptionMessage, rawJson);

    }
}
