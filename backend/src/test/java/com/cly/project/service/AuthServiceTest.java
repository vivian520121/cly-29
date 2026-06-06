package com.cly.project.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.cly.project.dto.LoginDTO;
import com.cly.project.dto.LoginVO;
import com.cly.project.entity.User;
import com.cly.project.enums.UserTypeEnum;
import com.cly.project.exception.BusinessException;
import com.cly.project.mapper.UserMapper;
import com.cly.project.util.JwtUtil;
import com.cly.project.util.UserContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import jakarta.servlet.http.HttpServletRequest;
import java.time.LocalDateTime;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("认证服务单元测试")
class AuthServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private JwtUtil jwtUtil;

    @Mock
    private StringRedisTemplate redisTemplate;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private HttpServletRequest request;

    @Mock
    private ValueOperations<String, String> valueOperations;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private LoginDTO loginDTO;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("admin");
        testUser.setPassword("$2a$10$encodedPasswordHash");
        testUser.setRealName("管理员");
        testUser.setAvatar("avatar.jpg");
        testUser.setUserType(UserTypeEnum.ADMIN.getCode());
        testUser.setStatus(1);

        loginDTO = new LoginDTO();
        loginDTO.setUsername("admin");
        loginDTO.setPassword("admin123");

        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
    }

    @Test
    @DisplayName("登录成功")
    void testLoginSuccess() {
        try (MockedStatic<UserContext> mocked = mockStatic(UserContext.class)) {
            when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);
            when(passwordEncoder.matches("admin123", testUser.getPassword())).thenReturn(true);
            when(jwtUtil.generateToken(1L, "admin")).thenReturn("test-token-123");
            when(jwtUtil.getExpiration()).thenReturn(3600000L);
            when(request.getRemoteAddr()).thenReturn("127.0.0.1");

            LoginVO result = authService.login(loginDTO);

            assertNotNull(result);
            assertEquals("test-token-123", result.getToken());
            assertEquals(1L, result.getUserId());
            assertEquals("admin", result.getUsername());
            assertEquals("管理员", result.getRealName());
            assertEquals("avatar.jpg", result.getAvatar());
            assertEquals(UserTypeEnum.ADMIN.getCode(), result.getUserType());

            verify(userMapper, times(1)).updateById(any(User.class));
            verify(valueOperations, times(2)).set(anyString(), anyString(), eq(3600000L), eq(TimeUnit.MILLISECONDS));
        }
    }

    @Test
    @DisplayName("登录失败 - 用户不存在")
    void testLoginUserNotFound() {
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> authService.login(loginDTO));

        assertEquals("用户名或密码错误", exception.getMessage());
        verify(userMapper, never()).updateById(any());
    }

    @Test
    @DisplayName("登录失败 - 密码错误")
    void testLoginPasswordError() {
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);
        when(passwordEncoder.matches("admin123", testUser.getPassword())).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class, () -> authService.login(loginDTO));

        assertEquals("用户名或密码错误", exception.getMessage());
        verify(userMapper, never()).updateById(any());
    }

    @Test
    @DisplayName("登录失败 - 账号已禁用")
    void testLoginAccountDisabled() {
        testUser.setStatus(0);
        when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);
        when(passwordEncoder.matches("admin123", testUser.getPassword())).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> authService.login(loginDTO));

        assertEquals("账号已被禁用，请联系管理员", exception.getMessage());
        verify(userMapper, never()).updateById(any());
    }

    @Test
    @DisplayName("登出成功 - 已登录用户")
    void testLogoutSuccess() {
        try (MockedStatic<UserContext> mocked = mockStatic(UserContext.class)) {
            mocked.when(UserContext::getUserId).thenReturn(1L);

            assertDoesNotThrow(() -> authService.logout());

            verify(redisTemplate, times(2)).delete(anyString());
        }
    }

    @Test
    @DisplayName("登出 - 未登录用户")
    void testLogoutNotLoggedIn() {
        try (MockedStatic<UserContext> mocked = mockStatic(UserContext.class)) {
            mocked.when(UserContext::getUserId).thenReturn(null);

            assertDoesNotThrow(() -> authService.logout());

            verify(redisTemplate, never()).delete(anyString());
        }
    }

    @Test
    @DisplayName("获取当前登录用户信息成功")
    void testGetCurrentUserSuccess() {
        try (MockedStatic<UserContext> mocked = mockStatic(UserContext.class)) {
            mocked.when(UserContext::getUserId).thenReturn(1L);
            when(userMapper.selectById(1L)).thenReturn(testUser);

            User result = authService.getCurrentUser();

            assertNotNull(result);
            assertEquals(1L, result.getId());
            assertEquals("admin", result.getUsername());
            assertNull(result.getPassword());
        }
    }

    @Test
    @DisplayName("获取当前登录用户 - 未登录")
    void testGetCurrentUserNotLoggedIn() {
        try (MockedStatic<UserContext> mocked = mockStatic(UserContext.class)) {
            mocked.when(UserContext::getUserId).thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class, () -> authService.getCurrentUser());

            assertEquals("用户未登录", exception.getMessage());
        }
    }

    @Test
    @DisplayName("获取当前登录用户 - 用户不存在")
    void testGetCurrentUserNotFound() {
        try (MockedStatic<UserContext> mocked = mockStatic(UserContext.class)) {
            mocked.when(UserContext::getUserId).thenReturn(999L);
            when(userMapper.selectById(999L)).thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class, () -> authService.getCurrentUser());

            assertEquals("用户不存在", exception.getMessage());
        }
    }

    @Test
    @DisplayName("刷新Token成功")
    void testRefreshTokenSuccess() {
        try (MockedStatic<UserContext> mocked = mockStatic(UserContext.class)) {
            mocked.when(UserContext::getUserId).thenReturn(1L);
            mocked.when(UserContext::getUsername).thenReturn("admin");
            mocked.when(UserContext::getUserType).thenReturn(UserTypeEnum.ADMIN.getCode());

            when(jwtUtil.generateToken(1L, "admin")).thenReturn("new-token-456");
            when(jwtUtil.getExpiration()).thenReturn(3600000L);

            String newToken = authService.refreshToken();

            assertNotNull(newToken);
            assertEquals("new-token-456", newToken);

            verify(valueOperations, times(2)).set(anyString(), anyString(), eq(3600000L), eq(TimeUnit.MILLISECONDS));
        }
    }

    @Test
    @DisplayName("刷新Token失败 - 用户未登录")
    void testRefreshTokenNotLoggedIn() {
        try (MockedStatic<UserContext> mocked = mockStatic(UserContext.class)) {
            mocked.when(UserContext::getUserId).thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class, () -> authService.refreshToken());

            assertEquals("用户未登录", exception.getMessage());
        }
    }

    @Test
    @DisplayName("登录成功 - 更新最后登录信息")
    void testLoginUpdatesLastLoginInfo() {
        try (MockedStatic<UserContext> mocked = mockStatic(UserContext.class)) {
            when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);
            when(passwordEncoder.matches("admin123", testUser.getPassword())).thenReturn(true);
            when(jwtUtil.generateToken(1L, "admin")).thenReturn("test-token-123");
            when(jwtUtil.getExpiration()).thenReturn(3600000L);
            when(request.getRemoteAddr()).thenReturn("192.168.1.100");

            authService.login(loginDTO);

            verify(userMapper, times(1)).updateById(argThat(user -> {
                assertNotNull(user.getLastLoginTime());
                assertTrue(user.getLastLoginTime().isBefore(LocalDateTime.now().plusSeconds(1)));
                assertEquals("192.168.1.100", user.getLastLoginIp());
                return true;
            }));
        }
    }

    @Test
    @DisplayName("登录成功 - 普通用户")
    void testLoginAsNormalUser() {
        try (MockedStatic<UserContext> mocked = mockStatic(UserContext.class)) {
            testUser.setUserType(UserTypeEnum.USER.getCode());
            when(userMapper.selectOne(any(LambdaQueryWrapper.class))).thenReturn(testUser);
            when(passwordEncoder.matches("admin123", testUser.getPassword())).thenReturn(true);
            when(jwtUtil.generateToken(1L, "admin")).thenReturn("test-token-123");
            when(jwtUtil.getExpiration()).thenReturn(3600000L);
            when(request.getRemoteAddr()).thenReturn("127.0.0.1");

            LoginVO result = authService.login(loginDTO);

            assertNotNull(result);
            assertEquals(UserTypeEnum.USER.getCode(), result.getUserType());
        }
    }

    @Test
    @DisplayName("登出 - 清除Redis中的Token")
    void testLogoutClearsRedisToken() {
        try (MockedStatic<UserContext> mocked = mockStatic(UserContext.class)) {
            mocked.when(UserContext::getUserId).thenReturn(1L);

            authService.logout();

            verify(redisTemplate).delete("user:token:1");
            verify(redisTemplate).delete("user:type:1");
        }
    }

    @Test
    @DisplayName("刷新Token - 重新设置Redis缓存")
    void testRefreshTokenResetsRedisCache() {
        try (MockedStatic<UserContext> mocked = mockStatic(UserContext.class)) {
            mocked.when(UserContext::getUserId).thenReturn(1L);
            mocked.when(UserContext::getUsername).thenReturn("admin");
            mocked.when(UserContext::getUserType).thenReturn(1);

            when(jwtUtil.generateToken(1L, "admin")).thenReturn("new-token");
            when(jwtUtil.getExpiration()).thenReturn(3600000L);

            authService.refreshToken();

            verify(valueOperations).set(eq("user:token:1"), eq("new-token"), eq(3600000L), eq(TimeUnit.MILLISECONDS));
            verify(valueOperations).set(eq("user:type:1"), eq("1"), eq(3600000L), eq(TimeUnit.MILLISECONDS));
        }
    }

    @Test
    @DisplayName("获取当前用户 - 密码字段置空")
    void testGetCurrentUserClearsPassword() {
        try (MockedStatic<UserContext> mocked = mockStatic(UserContext.class)) {
            mocked.when(UserContext::getUserId).thenReturn(1L);
            when(userMapper.selectById(1L)).thenReturn(testUser);

            User result = authService.getCurrentUser();

            assertNull(result.getPassword());
        }
    }
}
