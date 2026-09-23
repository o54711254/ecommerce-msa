package com.ecommerce.reviewservice.kafka.config

enum class KafkaTopic(val topicName: String) {

    REVIEW_CREATED(TopicName.REVIEW_CREATED)
    ;

    // object는 선언과 동시에 인스턴스 딱 하나 만드는 문법
    object TopicName {

        /* const는 컴파일 타임에 값이 확정된 상수, val은 한 번 할당 후 변경 불가 지만 런타임값
        * const는 object, companion object안쪽 혹은 top-level에서 사용 가능
         */
        const val REVIEW_CREATED = "review.created"
    }
}