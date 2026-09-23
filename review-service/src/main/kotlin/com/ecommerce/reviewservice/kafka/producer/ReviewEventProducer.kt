package com.ecommerce.reviewservice.kafka.producer

import com.ecommerce.reviewservice.kafka.config.KafkaTopic
import com.ecommerce.reviewservice.kafka.dto.ReviewCreatedEvent
import com.fasterxml.jackson.core.JsonProcessingException
import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.kafka.core.KafkaTemplate
import org.springframework.stereotype.Component

@Component
class ReviewEventProducer(val kafkaTemplate: KafkaTemplate<String, String>, val objectMapper: ObjectMapper) {

    fun sendReviewCreated(event: ReviewCreatedEvent) {
        try {

            kafkaTemplate.send(
                KafkaTopic.TopicName.REVIEW_CREATED,
                event.reviewId.toString(),  // 당장은 key값은 필요없지만, 나중에 순서가 보장되어야 할 일이 생길 수 있으므로 일단 reviewId로 넣기
                objectMapper.writeValueAsString(event)
            )
        } catch (e: JsonProcessingException) {
            throw RuntimeException(e)
        }
    }
}