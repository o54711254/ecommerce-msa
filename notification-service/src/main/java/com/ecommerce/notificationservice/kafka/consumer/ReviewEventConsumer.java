package com.ecommerce.notificationservice.kafka.consumer;

import com.ecommerce.notificationservice.client.ProductClient;
import com.ecommerce.notificationservice.domain.dto.req.CreateNotificationRequest;
import com.ecommerce.notificationservice.domain.service.NotificationService;
import com.ecommerce.notificationservice.kafka.config.KafkaTopic;
import com.ecommerce.notificationservice.kafka.dto.ReviewCreatedEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewEventConsumer {

    private final ObjectMapper objectMapper;
    private final ProductClient productClient;
    private final NotificationService notificationService;

    @KafkaListener(topics = KafkaTopic.TopicName.REVIEW_CREATED, groupId = "notificationGroup")
    public void handleReviewCreated(String rawJson) {
        ReviewCreatedEvent event;
        try {
            event = objectMapper.readValue(rawJson, ReviewCreatedEvent.class);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }

        log.info("[review.created] consumed: reviewId={}", event.reviewId());

        Long sellerId = productClient.getSellerId(event.productId());
        notificationService.createNotification(KafkaTopic.REVIEW_CREATED, new CreateNotificationRequest(sellerId, event));
    }
}
