package com.ecommerce.inventoryservice.kafka.producer;

import com.ecommerce.inventoryservice.kafka.config.KafkaTopic;
import com.ecommerce.inventoryservice.kafka.dto.InventoryDecreasedEvent;
import com.ecommerce.inventoryservice.kafka.dto.InventoryFailedEvent;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class InventoryEventProducerTest {

    @Mock private KafkaTemplate<String, String> kafkaTemplate;
    @Spy private ObjectMapper objectMapper = new ObjectMapper();
    @InjectMocks private InventoryEventProducer inventoryEventProducer;

    @Nested
    @DisplayName("sendInventoryDecreased - inventory.decreased 발행")
    class SendInventoryDecreasedTest {

        @Test
        void 올바른_토픽_키_JSON으로_발행() throws Exception {
            InventoryDecreasedEvent event = new InventoryDecreasedEvent(10L, 1L, 5000L);

            inventoryEventProducer.sendInventoryDecreased(event);

            ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
            verify(kafkaTemplate).send(
                    org.mockito.ArgumentMatchers.eq(KafkaTopic.TopicName.INVENTORY_DECREASED),
                    org.mockito.ArgumentMatchers.eq("10"),
                    jsonCaptor.capture());

            InventoryDecreasedEvent captured = objectMapper.readValue(jsonCaptor.getValue(), InventoryDecreasedEvent.class);
            assertThat(captured.orderId()).isEqualTo(10L);
            assertThat(captured.memberId()).isEqualTo(1L);
            assertThat(captured.amount()).isEqualTo(5000L);
        }
    }

    @Nested
    @DisplayName("sendInventoryFailed - inventory.failed 발행")
    class SendInventoryFailedTest {

        @Test
        void 올바른_토픽_키_JSON으로_발행() throws Exception {
            InventoryFailedEvent event = new InventoryFailedEvent(10L);

            inventoryEventProducer.sendInventoryFailed(event);

            ArgumentCaptor<String> jsonCaptor = ArgumentCaptor.forClass(String.class);
            verify(kafkaTemplate).send(
                    org.mockito.ArgumentMatchers.eq(KafkaTopic.TopicName.INVENTORY_FAILED),
                    org.mockito.ArgumentMatchers.eq("10"),
                    jsonCaptor.capture());

            InventoryFailedEvent captured = objectMapper.readValue(jsonCaptor.getValue(), InventoryFailedEvent.class);
            assertThat(captured.orderId()).isEqualTo(10L);
        }
    }
}
