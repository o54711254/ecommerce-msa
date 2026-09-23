package com.ecommerce.reviewservice.kafka.config

import org.apache.kafka.clients.admin.NewTopic
import org.apache.kafka.common.config.TopicConfig
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.kafka.config.TopicBuilder
import java.time.Duration

@Configuration
class KafkaConfig {

    companion object {
        val DELETED_PERIOD = Duration.ofDays(30).toMillis()
    }

    @Bean
    fun reviewCreatedTopic(): NewTopic =
        TopicBuilder.name(KafkaTopic.TopicName.REVIEW_CREATED)
            .partitions(3)
            .config(TopicConfig.RETENTION_MS_CONFIG, DELETED_PERIOD.toString())
            .build()
}