package com.ecommerce.reviewservice.client.member

import com.ecommerce.reviewservice.client.member.dto.MemberInfoResponse
import org.slf4j.LoggerFactory
import org.springframework.cloud.openfeign.FallbackFactory
import org.springframework.stereotype.Component

@Component
class MemberClientFallbackFactory : FallbackFactory<MemberClient> {

    private val log = LoggerFactory.getLogger(javaClass)

    override fun create(cause: Throwable?): MemberClient {
        log.error("member-service 호출 실패 - {}", cause?.message)  // ?. 앞의 값이 null이 아니면 뒤으 프로퍼티 호출, null이면 null 반환
        return object : MemberClient {
            override fun getMemberInfos(ids: List<Long>): List<MemberInfoResponse> {
                return emptyList()
            }
        }
    }
}