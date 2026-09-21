package com.ecommerce.reviewservice.client.member

import com.ecommerce.reviewservice.client.member.dto.MemberInfoResponse
import org.springframework.cloud.openfeign.FeignClient
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestParam

@FeignClient("member-service", fallbackFactory = MemberClientFallbackFactory::class)
interface MemberClient {

    @GetMapping("/api/v1/member/infos")
    fun getMemberInfos(@RequestParam ids: List<Long>): List<MemberInfoResponse>

}