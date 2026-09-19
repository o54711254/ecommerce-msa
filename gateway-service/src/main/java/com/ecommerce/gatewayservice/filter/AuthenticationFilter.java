package com.ecommerce.gatewayservice.filter;

import com.ecommerce.gatewayservice.global.exception.ErrorCode;
import com.ecommerce.gatewayservice.global.exception.ErrorResponse;
import com.ecommerce.gatewayservice.util.JwtUtil;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.Set;

// JWT 검증 후 X-Member-Id, X-Member-Role 헤더를 추가해 다운스트림으로 전달
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthenticationFilter implements GlobalFilter, Ordered {

    private static final Set<String> OPEN_ENDPOINTS = Set.of(
            "/login",
            "/join",
            "/join/seller",
            "/webhook"
    );

    // Swagger 관련 경로 (인증 없이 통과)
    private static final Set<String> SWAGGER_PREFIXES = Set.of(
            "/v3/api-docs",
            "/swagger-ui",
            "/swagger-resources",
            "/webjars"
    );

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String uri = exchange.getRequest().getURI().getPath();

        if (OPEN_ENDPOINTS.stream().anyMatch(uri::endsWith)
                || SWAGGER_PREFIXES.stream().anyMatch(uri::startsWith)
                || uri.equals("/swagger-ui.html")) {
            return chain.filter(exchange);
        }

        String authorizationHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
            return writeErrorResponse(exchange, ErrorCode.UNAUTHORIZED);
        }

        String token = authorizationHeader.substring(7);
        try {
            Long memberId = jwtUtil.getMemberId(token);
            String role = jwtUtil.getRole(token);

            // ServerHttpRequest는 불변 — mutate()로 헤더를 추가한 새 인스턴스 생성
            ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                    .header("X-Member-Id", memberId.toString())
                    .header("X-Member-Role", role)
                    .build();

            return chain.filter(exchange.mutate().request(mutatedRequest).build());
        } catch (JwtException e) {
            log.warn("JWT 검증 실패 - {}", e.getMessage());
            return writeErrorResponse(exchange, ErrorCode.UNAUTHORIZED);
        }
    }

    private Mono<Void> writeErrorResponse(ServerWebExchange exchange, ErrorCode errorCode) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(errorCode.getStatus());
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);

        byte[] bytes;
        try {
            bytes = objectMapper.writeValueAsBytes(ErrorResponse.of(errorCode));
        } catch (JsonProcessingException e) {
            bytes = new byte[0];
        }

        // WebFlux는 OutputStream 대신 DataBuffer 단위로 응답 body를 흘려보냄
        DataBuffer buffer = response.bufferFactory().wrap(bytes);
        return response.writeWith(Mono.just(buffer));
    }

    // 내장 필터(RouteToRequestUrlFilter: 10000)보다 먼저 실행되어야 라우팅 전에 인증 처리 가능
    @Override
    public int getOrder() {
        return -1;
    }
}
