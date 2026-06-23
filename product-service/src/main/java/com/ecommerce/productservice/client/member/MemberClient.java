package com.ecommerce.productservice.client.member;

import com.ecommerce.productservice.client.member.dto.SellerResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "member-service", fallbackFactory = MemberClientFallbackFactory.class)
public interface MemberClient {

    @GetMapping("/api/v1/member/{id}")
    SellerResponse getSeller(@PathVariable Long id);
}
