package com.ecommerce.productservice.infra.feign.member;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "member-service")
public interface MemberClient {

    @GetMapping("/api/v1/member/{id}")
    ResponseEntity<SellerResponse> getSeller(@PathVariable Long id);
}
