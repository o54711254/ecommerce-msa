package com.ecommerce.inventoryservice.kafka.producer;

import com.ecommerce.inventoryservice.kafka.dto.InventoryDecreasedEvent;
import com.ecommerce.inventoryservice.kafka.dto.InventoryFailedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class InventoryEventProducer {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper objectMapper;

    public void sendInventoryDecreased(InventoryDecreasedEvent event) {
        try {
            kafkaTemplate.send("inventory.decreased", String.valueOf(event.orderId()),
                    objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendInventoryFailed(InventoryFailedEvent event) {
        try {
            kafkaTemplate.send("inventory.failed", String.valueOf(event.orderId()),
                    objectMapper.writeValueAsString(event));
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }
}
