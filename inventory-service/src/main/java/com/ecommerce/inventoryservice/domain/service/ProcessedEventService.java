package com.ecommerce.inventoryservice.domain.service;

import com.ecommerce.inventoryservice.domain.entity.InventoryEventType;
import com.ecommerce.inventoryservice.domain.entity.ProcessedEvent;
import com.ecommerce.inventoryservice.domain.repository.ProcessedEventRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProcessedEventService {

    private final ProcessedEventRepository processedEventRepository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public boolean saveOrSkipOrderEvent(InventoryEventType eventType, Long orderId) {
        if (processedEventRepository.existsByEventTypeAndOrderId(eventType, orderId)) {
            log.info("중복 이벤트 스킵. 이벤트 타입 - {}, 주문번호 - {}", eventType, orderId);
            return false;
        }
        processedEventRepository.save(new ProcessedEvent(eventType, orderId));
        return true;
    }
}
