package com.ecommerce.memberservice.dto.req;

import com.ecommerce.memberservice.entity.Member;
import com.ecommerce.memberservice.entity.Role;

public record JoinMemberRequest(
        String email,
        String name,
        String password,
        String address
) {
    public Member toEntity(Role role, String encodedPassword) {
        return new Member(email, role, name, encodedPassword, address);
    }
}
