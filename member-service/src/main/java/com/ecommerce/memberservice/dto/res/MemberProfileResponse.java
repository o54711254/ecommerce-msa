package com.ecommerce.memberservice.dto.res;

import com.ecommerce.memberservice.entity.Role;

public record MemberProfileResponse(
        String email,
        Role role,
        String name,
        String address
) {
}
