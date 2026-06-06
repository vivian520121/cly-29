package com.cly.project.service;

import com.cly.project.dto.LoginDTO;
import com.cly.project.dto.LoginVO;
import com.cly.project.entity.User;
import com.cly.project.exception.BusinessException;
import com.cly.project.mapper.UserMapper;
import com.cly.project.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("认证服务单元测试")
class AuthServiceTest {

    @Mock
    private UserMapper userMapper;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private User testUser;
    private LoginDTO loginDTO;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("admin");
        testUser.setPassword("$2a$10$encodedPassword");
        testUser.setRealName("管理员");
        testUser.setStatus(1);
        testUser.setUserType(1);

        loginDTO = new LoginDTO();
        loginDTO.setUsername("admin");
        loginDTO.setPassword("password123");
    }

    @Test
    @DisplayName("登录成功")
    void testLoginSuccess() {
        when(userMapper.selectOne(any())).thenReturn(testUser);
        when(passwordEncoder.matches("password123", testUser.getPassword())).thenReturn(true);

        try (MockedStatic<JwtUtil> jwtUtilMock = mockStatic(JwtUtil.class)) {
            jwtUtilMock.when(() -> JwtUtil.generateToken(testUser)).thenReturn("mock-jwt-token");

            LoginVO result = authService.login(loginDTO);

            assertNotNull(result);
            assertNotNull(result.getToken());
            assertEquals("mock-jwt-token", result.getToken());
            assertNotNull(result.getUserInfo());
            assertEquals("admin", result.getUserInfo().getUsername());
            assertEquals("管理员", result.getUserInfo().getRealName());
            verify(userMapper, times(1)).selectOne(any());
        }
    }

    @Test
    @DisplayName("登录失败 - 用户不存在")
    void testLoginUserNotFound() {
        when(userMapper.selectOne(any())).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.login(loginDTO);
        });

        assertEquals("用户名或密码错误", exception.getMessage());
        verify(userMapper, times(1)).selectOne(any());
    }

    @Test
    @DisplayName("登录失败 - 密码错误")
    void testLoginWrongPassword() {
        when(userMapper.selectOne(any())).thenReturn(testUser);
        when(passwordEncoder.matches("password123", testUser.getPassword())).thenReturn(false);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.login(loginDTO);
        });

        assertEquals("用户名或密码错误", exception.getMessage());
        verify(userMapper, times(1)).selectOne(any());
        verify(passwordEncoder, times(1)).matches("password123", testUser.getPassword());
    }

    @Test
    @DisplayName("登录失败 - 用户已禁用")
    void testLoginUserDisabled() {
        testUser.setStatus(0);
        when(userMapper.selectOne(any())).thenReturn(testUser);
        when(passwordEncoder.matches("password123", testUser.getPassword())).thenReturn(true);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.login(loginDTO);
        });

        assertEquals("账号已被禁用，请联系管理员", exception.getMessage());
    }

    @Test
    @DisplayName("登录参数为空 - 用户名")
    void testLoginWithEmptyUsername() {
        loginDTO.setUsername("");

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.login(loginDTO);
        });

        assertEquals("用户名不能为空", exception.getMessage());
    }

    @Test
    @DisplayName("登录参数为空 - 密码")
    void testLoginWithEmptyPassword() {
        loginDTO.setPassword("");

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.login(loginDTO);
        });

        assertEquals("密码不能为空", exception.getMessage());
    }

    @Test
    @DisplayName("登出成功")
    void testLogoutSuccess() {
        assertDoesNotThrow(() -> authService.logout());
    }

    @Test
    @DisplayName("获取当前用户信息")
    void testGetCurrentUser() {
        try (MockedStatic<com.cly.project.util.UserContext> userContextMock = 
             mockStatic(com.cly.project.util.UserContext.class)) {
            
            com.cly.project.util.UserContext.CurrentUser currentUser = 
                new com.cly.project.util.UserContext.CurrentUser();
            currentUser.setId(1L);
            currentUser.setUsername("admin");
            currentUser.setRealName("管理员");
            
            userContextMock.when(() -> com.cly.project.util.UserContext.getCurrentUser())
                .thenReturn(currentUser);
            when(userMapper.selectById(1L)).thenReturn(testUser);

            User result = authService.getCurrentUser();

            assertNotNull(result);
            assertEquals("admin", result.getUsername());
            assertEquals("管理员", result.getRealName());
            assertNull(result.getPassword());
        }
    }

    @Test
    @DisplayName("获取当前用户 - 未登录")
    void testGetCurrentUserNotLoggedIn() {
        try (MockedStatic<com.cly.project.util.UserContext> userContextMock = 
             mockStatic(com.cly.project.util.UserContext.class)) {
            
            userContextMock.when(() -> com.cly.project.util.UserContext.getCurrentUser())
                .thenReturn(null);

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                authService.getCurrentUser();
            });

            assertEquals("用户未登录", exception.getMessage());
        }
    }

    @Test
    @DisplayName("修改密码成功")
    void testChangePasswordSuccess() {
        try (MockedStatic<com.cly.project.util.UserContext> userContextMock = 
             mockStatic(com.cly.project.util.UserContext.class)) {
            
            com.cly.project.util.UserContext.CurrentUser currentUser = 
                new com.cly.project.util.UserContext.CurrentUser();
            currentUser.setId(1L);
            
            userContextMock.when(() -> com.cly.project.util.UserContext.getCurrentUser())
                .thenReturn(currentUser);
            when(userMapper.selectById(1L)).thenReturn(testUser);
            when(passwordEncoder.matches("oldPassword", testUser.getPassword())).thenReturn(true);
            when(passwordEncoder.encode("newPassword")).thenReturn("$2a$10$newEncodedPassword");
            when(userMapper.updateById(any(User.class))).thenReturn(1);

            boolean result = authService.changePassword("oldPassword", "newPassword");

            assertTrue(result);
            verify(userMapper, times(1)).updateById(any(User.class));
        }
    }

    @Test
    @DisplayName("修改密码失败 - 旧密码错误")
    void testChangePasswordWrongOldPassword() {
        try (MockedStatic<com.cly.project.util.UserContext> userContextMock = 
             mockStatic(com.cly.project.util.UserContext.class)) {
            
            com.cly.project.util.UserContext.CurrentUser currentUser = 
                new com.cly.project.util.UserContext.CurrentUser();
            currentUser.setId(1L);
            
            userContextMock.when(() -> com.cly.project.util.UserContext.getCurrentUser())
                .thenReturn(currentUser);
            when(userMapper.selectById(1L)).thenReturn(testUser);
            when(passwordEncoder.matches("wrongPassword", testUser.getPassword())).thenReturn(false);

            BusinessException exception = assertThrows(BusinessException.class, () -> {
                authService.changePassword("wrongPassword", "newPassword");
            });

            assertEquals("原密码错误", exception.getMessage());
        }
    }

    @Test
    @DisplayName("修改密码 - 新密码不能为空")
    void testChangePasswordEmptyNewPassword() {
        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.changePassword("oldPassword", "");
        });

        assertEquals("新密码不能为空", exception.getMessage());
    }

    @Test
    @DisplayName("重置密码成功")
    void testResetPasswordSuccess() {
        when(userMapper.selectById(1L)).thenReturn(testUser);
        when(passwordEncoder.encode("newPassword123")).thenReturn("$2a$10$newEncodedPassword");
        when(userMapper.updateById(any(User.class))).thenReturn(1);

        boolean result = authService.resetPassword(1L, "newPassword123");

        assertTrue(result);
        verify(userMapper, times(1)).updateById(any(User.class));
    }

    @Test
    @DisplayName("重置密码 - 用户不存在")
    void testResetPasswordUserNotFound() {
        when(userMapper.selectById(999L)).thenReturn(null);

        BusinessException exception = assertThrows(BusinessException.class, () -> {
            authService.resetPassword(999L, "newPassword");
        });

        assertEquals("用户不存在", exception.getMessage());
    }
}
