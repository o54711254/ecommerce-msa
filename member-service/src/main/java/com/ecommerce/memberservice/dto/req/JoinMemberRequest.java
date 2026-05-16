package com.ecommerce.memberservice.dto.req;

import com.ecommerce.memberservice.entity.Member;

public record JoinMemberRequest(
        String email,
        String name,
        String password,
        String address
) {
    public Member toEntity(String encodedPassword){
        return new Member(email, name, encodedPassword, address);
    }
}
