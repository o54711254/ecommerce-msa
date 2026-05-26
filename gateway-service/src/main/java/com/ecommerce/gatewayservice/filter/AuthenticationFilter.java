package com.ecommerce.gatewayservice.filter;

import com.ecommerce.gatewayservice.global.exception.ErrorCode;
import com.ecommerce.gatewayservice.global.exception.ErrorResponse;
import com.ecommerce.gatewayservice.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 * 게이트웨이 인증 필터
 * <p>
 * 모든 요청은 이 필터를 먼저 통과한다.
 * - 공개 엔드포인트(/login, /join)는 토큰 없이 통과
 * - 나머지 요청은 JWT를 검증하고, 성공 시 memberId를 X-Member-Id 헤더에 담아 다운스트림 서비스로 전달
 * <p>
 * 다운스트림 서비스(member-service, order-service 등)는 X-Member-Id 헤더만 보고 요청자를 식별한다.
 * 토큰 검증은 게이트웨이에서만 하므로 각 서비스는 JWT 로직이 필요 없다.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. 공개 엔드포인트는 토큰 검증 없이 통과
        String uri = request.getRequestURI();
        if (uri.endsWith("/login") || uri.endsWith("/join") || uri.endsWith("/join/seller")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Authorization 헤더 존재 여부 확인
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader == null) {
            writeErrorResponse(response, ErrorCode.UNAUTHORIZED);
            return;
        }

        // 3. Bearer 접두사 확인 후 토큰 추출
        if (!authorizationHeader.startsWith("Bearer ")) {
            writeErrorResponse(response, ErrorCode.UNAUTHORIZED);
            return;
        }
        String token = authorizationHeader.substring(7);

        // 4. 토큰 유효성 검증
        try {
            Long memberId = jwtUtil.getMemberId(token);
            String role = jwtUtil.getRole(token);

            // 5. 검증 성공 시 memberId를 헤더에 심어서 다음 서비스로 전달
            // 각 서비스는 X-Member-Id 헤더를 믿고 그대로 사용한다
            Map<String, String> headerMap = new HashMap<>();
            headerMap.put("X-Member-Id", memberId.toString());
            headerMap.put("X-Member-Role", role);
            filterChain.doFilter(new AddHeaderRequestWrapper(request, headerMap), response);

        } catch (JwtException e) {
            log.warn("JWT 검증 실패 - {}", e.getMessage());
            writeErrorResponse(response, ErrorCode.UNAUTHORIZED);
        }
    }

    private void writeErrorResponse(HttpServletResponse response, ErrorCode errorCode) throws IOException {
        response.setStatus(errorCode.getStatus().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(ErrorResponse.of(errorCode)));
    }

    // HttpServletRequest에 커스텀 헤더를 추가하기 위한 래퍼
    private static class AddHeaderRequestWrapper extends HttpServletRequestWrapper {

        private final Map<String, String> customHeaders = new HashMap<>();

        public AddHeaderRequestWrapper(HttpServletRequest request, Map<String, String> customHeaders) {
            super(request);
            this.customHeaders.putAll(customHeaders);
        }

        @Override
        public String getHeader(String name) {
            if (customHeaders.containsKey(name)) return customHeaders.get(name);
            return super.getHeader(name);
        }

        @Override
        public Enumeration<String> getHeaders(String name) {
            if (customHeaders.containsKey(name))
                return Collections.enumeration(Collections.singleton(customHeaders.get(name)));
            return super.getHeaders(name);
        }

        @Override
        public Enumeration<String> getHeaderNames() {
            var names = Collections.list(super.getHeaderNames());
            names.addAll(customHeaders.keySet());
            return Collections.enumeration(names);
        }
    }
}
