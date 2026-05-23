package com.ecommerce.productservice.client.member;

import com.ecommerce.productservice.client.member.dto.SellerResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Component
public class MemberClientFallback implements MemberClient {

    @Override
    public ResponseEntity<SellerResponse> getSeller(Long id) {
        return ResponseEntity.ok(new SellerResponse("unknown", "unknown"));
    }
}
