package com.cly.project.util;

import com.cly.project.entity.User;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("JWT工具类测试")
class JwtUtilTest {

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User();
        testUser.setId(1L);
        testUser.setUsername("testuser");
        testUser.setRealName("测试用户");
        testUser.setUserType(1);
    }

    @Test
    @DisplayName("生成Token")
    void testGenerateToken() {
        String token = JwtUtil.generateToken(testUser);

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3);
    }

    @Test
    @DisplayName("解析Token")
    void testParseToken() {
        String token = JwtUtil.generateToken(testUser);

        Claims claims = JwtUtil.parseToken(token);

        assertNotNull(claims);
        assertEquals("1", claims.getSubject());
        assertEquals("testuser", claims.get("username"));
        assertEquals("测试用户", claims.get("realName"));
        assertEquals(1, claims.get("userType"));
    }

    @Test
    @DisplayName("从Token获取用户ID")
    void testGetUserIdFromToken() {
        String token = JwtUtil.generateToken(testUser);

        Long userId = JwtUtil.getUserId(token);

        assertNotNull(userId);
        assertEquals(1L, userId);
    }

    @Test
    @DisplayName("从Token获取用户名")
    void testGetUsernameFromToken() {
        String token = JwtUtil.generateToken(testUser);

        String username = JwtUtil.getUsername(token);

        assertNotNull(username);
        assertEquals("testuser", username);
    }

    @Test
    @DisplayName("验证Token有效性")
    void testValidateToken() {
        String token = JwtUtil.generateToken(testUser);

        boolean isValid = JwtUtil.validateToken(token);

        assertTrue(isValid);
    }

    @Test
    @DisplayName("验证无效Token")
    void testValidateInvalidToken() {
        boolean isValid = JwtUtil.validateToken("invalid.token.here");

        assertFalse(isValid);
    }

    @Test
    @DisplayName("验证空Token")
    void testValidateEmptyToken() {
        assertFalse(JwtUtil.validateToken(null));
        assertFalse(JwtUtil.validateToken(""));
        assertFalse(JwtUtil.validateToken("   "));
    }

    @Test
    @DisplayName("验证过期Token")
    void testValidateExpiredToken() throws InterruptedException {
        String token = JwtUtil.generateToken(testUser);

        Thread.sleep(1000);

        assertNotNull(JwtUtil.parseToken(token));
    }

    @Test
    @DisplayName("解析损坏的Token返回null")
    void testParseMalformedToken() {
        Claims claims = JwtUtil.parseToken("malformed.token");

        assertNull(claims);
    }

    @Test
    @DisplayName("从损坏的Token获取用户ID返回null")
    void testGetUserIdFromMalformedToken() {
        Long userId = JwtUtil.getUserId("malformed.token");

        assertNull(userId);
    }

    @Test
    @DisplayName("从损坏的Token获取用户名返回null")
    void testGetUsernameFromMalformedToken() {
        String username = JwtUtil.getUsername("malformed.token");

        assertNull(username);
    }

    @Test
    @DisplayName("Token包含所有必要字段")
    void testTokenContainsAllFields() {
        testUser.setId(123L);
        testUser.setUsername("john_doe");
        testUser.setRealName("John Doe");
        testUser.setUserType(2);

        String token = JwtUtil.generateToken(testUser);
        Claims claims = JwtUtil.parseToken(token);

        assertEquals("123", claims.getSubject());
        assertEquals("john_doe", claims.get("username"));
        assertEquals("John Doe", claims.get("realName"));
        assertEquals(2, claims.get("userType"));
    }
}
