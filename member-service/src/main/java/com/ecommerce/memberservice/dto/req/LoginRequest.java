package com.ecommerce.memberservice.dto.req;

public record LoginRequest(
        String email,
        String password
) {
}
