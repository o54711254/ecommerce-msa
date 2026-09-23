package com.ecommerce.notificationservice.domain.service;

import com.ecommerce.notificationservice.domain.entity.ProcessedEvent;
import com.ecommerce.notificationservice.domain.repository.ProcessedEventRepository;
import com.ecommerce.notificationservice.kafka.config.KafkaTopic;
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
    public boolean saveOrSkipEvent(KafkaTopic kafkaTopic, Long targetId) {
        if (processedEventRepository.existsByKafkaTopicAndTargetId(kafkaTopic, targetId)) {
            log.info("중복 이벤트 스킵. 이벤트 타입 - {}, 대상 ID - {}", kafkaTopic, targetId);
            return false;
        }
        processedEventRepository.save(new ProcessedEvent(kafkaTopic, targetId));
        return true;
    }
}
