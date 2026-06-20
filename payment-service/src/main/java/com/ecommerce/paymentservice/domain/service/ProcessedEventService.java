package com.ecommerce.paymentservice.domain.service;

import com.ecommerce.paymentservice.domain.entity.ProcessedEvent;
import com.ecommerce.paymentservice.domain.repository.ProcessedEventRepository;
import com.ecommerce.paymentservice.kafka.config.KafkaTopic;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessedEventService {

    private final ProcessedEventRepository processedEventRepository;

    @Transactional
    public boolean saveOrSkipOrderEvent(KafkaTopic kafkaTopic, Long orderId) {
        if (processedEventRepository.existsByKafkaTopicAndOrderId(kafkaTopic, orderId)) {
            log.info("중복 이벤트 스킵. 이벤트 타입 - {}, 주문번호 - {}", kafkaTopic, orderId);
            return false;
        }
        processedEventRepository.save(new ProcessedEvent(kafkaTopic, orderId));
        return true;
    }
}
