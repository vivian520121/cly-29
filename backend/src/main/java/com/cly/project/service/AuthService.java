package com.cly.project.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cly.project.dto.LoginDTO;
import com.cly.project.dto.LoginVO;
import com.cly.project.entity.User;
import com.cly.project.exception.BusinessException;
import com.cly.project.mapper.UserMapper;
import com.cly.project.util.JwtUtil;
import com.cly.project.util.UserContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserMapper userMapper;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;
    private final PasswordEncoder passwordEncoder;
    private final HttpServletRequest request;

    public LoginVO login(LoginDTO loginDTO) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getUsername, loginDTO.getUsername());
        User user = userMapper.selectOne(queryWrapper);

        if (user == null) {
            throw new BusinessException("用户名或密码错误");
        }

        if (!passwordEncoder.matches(loginDTO.getPassword(), user.getPassword())) {
            throw new BusinessException("用户名或密码错误");
        }

        if (user.getStatus() != null && user.getStatus() != 1) {
            throw new BusinessException("账号已被禁用，请联系管理员");
        }

        String token = jwtUtil.generateToken(user.getId(), user.getUsername());

        String tokenKey = "user:token:" + user.getId();
        String typeKey = "user:type:" + user.getId();
        redisTemplate.opsForValue().set(tokenKey, token, jwtUtil.getExpiration(), TimeUnit.MILLISECONDS);
        redisTemplate.opsForValue().set(typeKey, String.valueOf(user.getUserType()), jwtUtil.getExpiration(), TimeUnit.MILLISECONDS);

        user.setLastLoginTime(LocalDateTime.now());
        user.setLastLoginIp(getClientIp());
        userMapper.updateById(user);

        return new LoginVO(
                token,
                user.getId(),
                user.getUsername(),
                user.getRealName(),
                user.getAvatar(),
                user.getUserType()
        );
    }

    public void logout() {
        Long userId = UserContext.getUserId();
        if (userId != null) {
            redisTemplate.delete("user:token:" + userId);
            redisTemplate.delete("user:type:" + userId);
        }
    }

    public User getCurrentUser() {
        Long userId = UserContext.getUserId();
        if (userId == null) {
            throw new BusinessException("用户未登录");
        }
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new BusinessException("用户不存在");
        }
        user.setPassword(null);
        return user;
    }

    public String refreshToken() {
        Long userId = UserContext.getUserId();
        String username = UserContext.getUsername();
        Integer userType = UserContext.getUserType();

        if (userId == null || username == null) {
            throw new BusinessException("用户未登录");
        }

        String newToken = jwtUtil.generateToken(userId, username);

        String tokenKey = "user:token:" + userId;
        String typeKey = "user:type:" + userId;
        redisTemplate.opsForValue().set(tokenKey, newToken, jwtUtil.getExpiration(), TimeUnit.MILLISECONDS);
        redisTemplate.opsForValue().set(typeKey, String.valueOf(userType), jwtUtil.getExpiration(), TimeUnit.MILLISECONDS);

        return newToken;
    }

    private String getClientIp() {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("X-Real-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        return ip;
    }
}
