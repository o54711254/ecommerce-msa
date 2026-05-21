package com.ecommerce.productservice.infra.feign.member;

import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class MemberClientFallback implements MemberClient {

    @Override
    public ResponseEntity<SellerResponse> getSeller(Long id) {
        return ResponseEntity.ok(new SellerResponse("unknown", "unknown"));
    }
}
