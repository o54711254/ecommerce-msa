package com.ecommerce.memberservice.dto.res;

public record MemberResponse(
        Long memberId,
        String email
) {
}
