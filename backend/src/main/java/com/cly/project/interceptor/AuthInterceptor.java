package com.cly.project.interceptor;

import com.cly.project.common.Result;
import com.cly.project.util.JwtUtil;
import com.cly.project.util.UserContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Slf4j
@Component
@RequiredArgsConstructor
public class AuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equals(request.getMethod())) {
            return true;
        }

        String token = request.getHeader(jwtUtil.getHeader());
        if (token == null || token.isEmpty()) {
            return unauthorized(response, "未登录，请先登录");
        }

        if (token.startsWith(jwtUtil.getPrefix())) {
            token = token.substring(jwtUtil.getPrefix().length()).trim();
        }

        if (!jwtUtil.validateToken(token)) {
            return unauthorized(response, "Token无效或已过期");
        }

        Claims claims = jwtUtil.parseToken(token);
        Long userId = claims.get("userId", Long.class);
        String username = claims.get("username", String.class);

        String redisKey = "user:token:" + userId;
        String cachedToken = redisTemplate.opsForValue().get(redisKey);
        if (cachedToken == null || !cachedToken.equals(token)) {
            return unauthorized(response, "登录已过期，请重新登录");
        }

        String userTypeStr = redisTemplate.opsForValue().get("user:type:" + userId);
        Integer userType = userTypeStr != null ? Integer.parseInt(userTypeStr) : 2;

        UserContext.setCurrentUser(userId, username, userType);

        return true;
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserContext.clear();
    }

    private boolean unauthorized(HttpServletResponse response, String message) throws Exception {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write(objectMapper.writeValueAsString(Result.error(Result.UNAUTHORIZED, message)));
        return false;
    }
}
