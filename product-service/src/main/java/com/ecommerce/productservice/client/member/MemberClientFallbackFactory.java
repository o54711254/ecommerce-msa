package com.ecommerce.productservice.client.member;

import com.ecommerce.productservice.client.member.dto.SellerResponse;
import com.ecommerce.productservice.global.exception.ExternalServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class MemberClientFallbackFactory implements FallbackFactory<MemberClient> {

    @Override
    public MemberClient create(Throwable cause) {
        log.error("member-service 호출 실패 - {}", cause.getMessage());
        return new MemberClient() {
            @Override
            public ResponseEntity<SellerResponse> getSeller(Long id) {
                log.warn("member-service 불가 - 판매자 정보 없이 응답");
                return ResponseEntity.ok(new SellerResponse(null, null));
            }
        };
    }
}
