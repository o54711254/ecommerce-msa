package com.ecommerce.memberservice.dto.res;

public record MemberProfileResponse(
        String email,
        String name,
        String address
) {
}
