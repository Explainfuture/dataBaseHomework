package com.hnu.campus.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hnu.campus.dto.common.ApiResponse;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.util.List;

@Component
public class AuthInterceptor implements HandlerInterceptor {
    private static final String AUTH_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final AntPathMatcher pathMatcher = new AntPathMatcher();
    private final List<PublicEndpoint> publicEndpoints = List.of(
            new PublicEndpoint(HttpMethod.POST, "/api/v1/auth/**"),
            new PublicEndpoint(HttpMethod.GET, "/api/v1/posts"),
            new PublicEndpoint(HttpMethod.GET, "/api/v1/posts/search"),
            new PublicEndpoint(HttpMethod.GET, "/api/v1/posts/hot"),
            new PublicEndpoint(HttpMethod.GET, "/api/v1/posts/*"),
            new PublicEndpoint(HttpMethod.GET, "/api-docs/**"),
            new PublicEndpoint(HttpMethod.GET, "/v3/api-docs/**"),
            new PublicEndpoint(HttpMethod.GET, "/swagger-ui/**")
    );

    public AuthInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        String path = request.getRequestURI();
        String method = request.getMethod();
        if (HttpMethod.OPTIONS.matches(method)) {
            return true;
        }
        boolean isPublic = publicEndpoints.stream()
                .anyMatch(endpoint -> endpoint.matches(pathMatcher, method, path));

        String authHeader = request.getHeader(AUTH_HEADER);
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            if (isPublic) {
                return true;
            }
            writeUnauthorized(response, "缺少认证信息");
            return false;
        }

        String token = authHeader.substring(BEARER_PREFIX.length()).trim();
        try {
            Claims claims = jwtUtil.parseToken(token);
            Long userId = jwtUtil.getUserId(claims);
            String role = jwtUtil.getRole(claims);
            CurrentUserContext.setUser(userId, role);
            return true;
        } catch (Exception ex) {
            if (isPublic) {
                return true;
            }
            writeUnauthorized(response, "认证信息无效");
            return false;
        }
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) {
        CurrentUserContext.clear();
    }

    private void writeUnauthorized(HttpServletResponse response, String message) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        ApiResponse<Void> body = ApiResponse.error(401, message);
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }

    private record PublicEndpoint(HttpMethod method, String pattern) {
        boolean matches(AntPathMatcher matcher, String requestMethod, String path) {
            return method.matches(requestMethod) && matcher.match(pattern, path);
        }
    }
}
