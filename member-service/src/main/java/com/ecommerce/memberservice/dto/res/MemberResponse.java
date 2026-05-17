package com.ecommerce.memberservice.dto.res;

import com.ecommerce.memberservice.entity.Role;

public record MemberResponse(
        Long memberId,
        Role role,
        String email
) {
}
