package com.ecommerce.gatewayservice.filter;

import com.ecommerce.gatewayservice.util.JwtUtil;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class AuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        // 1. 공개 엔드포인트는 토큰 검증 없이 통과
        if (request.getRequestURI().endsWith("/login") || request.getRequestURI().endsWith("/join")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 2. Authorization 헤더 존재 여부 확인
        String authorizationHeader = request.getHeader(HttpHeaders.AUTHORIZATION);
        if (authorizationHeader == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No authorization header");
            return;
        }

        // 3. Bearer 접두사 확인 후 토큰 추출
        if (!authorizationHeader.startsWith("Bearer ")) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token format");
            return;
        }
        String token = authorizationHeader.substring(7);

        // 4. 토큰 유효성 검증
        try {
            Long memberId = jwtUtil.getMemberId(token);

            // 5. 검증 성공 시 memberId를 헤더에 심어서 다음 서비스로 전달
            // 각 서비스는 X-Member-Id 헤더를 믿고 그대로 사용한다
            filterChain.doFilter(new AddHeaderRequestWrapper(request, "X-Member-Id", memberId.toString()), response);
        } catch (JwtException e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED, "Invalid token: " + e.getMessage());
        }
    }

    // HttpServletRequest에 커스텀 헤더를 추가하기 위한 래퍼
    private static class AddHeaderRequestWrapper extends HttpServletRequestWrapper {

        private final Map<String, String> customHeaders = new HashMap<>();

        public AddHeaderRequestWrapper(HttpServletRequest request, String name, String value) {
            super(request);
            customHeaders.put(name, value);
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
