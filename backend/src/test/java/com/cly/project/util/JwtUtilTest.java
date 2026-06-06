package com.cly.project.util;

import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("JWT工具类单元测试")
class JwtUtilTest {

    @InjectMocks
    private JwtUtil jwtUtil;

    private static final String TEST_SECRET = "test-secret-key-1234567890-test-secret-key";
    private static final long TEST_EXPIRATION = 3600000L;
    private static final String TEST_HEADER = "Authorization";
    private static final String TEST_PREFIX = "Bearer ";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(jwtUtil, "secret", TEST_SECRET);
        ReflectionTestUtils.setField(jwtUtil, "expiration", TEST_EXPIRATION);
        ReflectionTestUtils.setField(jwtUtil, "header", TEST_HEADER);
        ReflectionTestUtils.setField(jwtUtil, "prefix", TEST_PREFIX);
    }

    @Test
    @DisplayName("生成Token成功")
    void testGenerateToken() {
        String token = jwtUtil.generateToken(1L, "testuser");

        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.length() > 0);
    }

    @Test
    @DisplayName("生成Token - 不同用户生成不同Token")
    void testGenerateTokenDifferentUsers() {
        String token1 = jwtUtil.generateToken(1L, "user1");
        String token2 = jwtUtil.generateToken(2L, "user2");

        assertNotNull(token1);
        assertNotNull(token2);
        assertNotEquals(token1, token2);
    }

    @Test
    @DisplayName("解析Token成功")
    void testParseToken() {
        String token = jwtUtil.generateToken(1L, "testuser");

        Claims claims = jwtUtil.parseToken(token);

        assertNotNull(claims);
        assertEquals(1L, claims.get("userId", Long.class));
        assertEquals("testuser", claims.get("username", String.class));
    }

    @Test
    @DisplayName("解析Token - 无效Token返回null")
    void testParseTokenInvalid() {
        Claims claims = jwtUtil.parseToken("invalid-token");

        assertNull(claims);
    }

    @Test
    @DisplayName("解析Token - null返回null")
    void testParseTokenNull() {
        Claims claims = jwtUtil.parseToken(null);

        assertNull(claims);
    }

    @Test
    @DisplayName("验证Token成功")
    void testValidateToken() {
        String token = jwtUtil.generateToken(1L, "testuser");

        boolean isValid = jwtUtil.validateToken(token);

        assertTrue(isValid);
    }

    @Test
    @DisplayName("验证Token - 无效Token")
    void testValidateTokenInvalid() {
        boolean isValid = jwtUtil.validateToken("invalid-token");

        assertFalse(isValid);
    }

    @Test
    @DisplayName("检查Token是否过期 - 未过期")
    void testIsTokenExpiredNotExpired() {
        String token = jwtUtil.generateToken(1L, "testuser");

        boolean isExpired = jwtUtil.isTokenExpired(token);

        assertFalse(isExpired);
    }

    @Test
    @DisplayName("检查Token是否过期 - 无效Token")
    void testIsTokenExpiredInvalid() {
        boolean isExpired = jwtUtil.isTokenExpired("invalid-token");

        assertTrue(isExpired);
    }

    @Test
    @DisplayName("从Token获取用户ID成功")
    void testGetUserIdFromToken() {
        String token = jwtUtil.generateToken(100L, "testuser");

        Long userId = jwtUtil.getUserIdFromToken(token);

        assertNotNull(userId);
        assertEquals(100L, userId);
    }

    @Test
    @DisplayName("从Token获取用户ID - 无效Token")
    void testGetUserIdFromTokenInvalid() {
        Long userId = jwtUtil.getUserIdFromToken("invalid-token");

        assertNull(userId);
    }

    @Test
    @DisplayName("从Token获取用户名成功")
    void testGetUsernameFromToken() {
        String token = jwtUtil.generateToken(1L, "zhangsan");

        String username = jwtUtil.getUsernameFromToken(token);

        assertNotNull(username);
        assertEquals("zhangsan", username);
    }

    @Test
    @DisplayName("从Token获取用户名 - 无效Token")
    void testGetUsernameFromTokenInvalid() {
        String username = jwtUtil.getUsernameFromToken("invalid-token");

        assertNull(username);
    }

    @Test
    @DisplayName("获取Header配置")
    void testGetHeader() {
        String header = jwtUtil.getHeader();

        assertEquals(TEST_HEADER, header);
    }

    @Test
    @DisplayName("获取Prefix配置")
    void testGetPrefix() {
        String prefix = jwtUtil.getPrefix();

        assertEquals(TEST_PREFIX, prefix);
    }

    @Test
    @DisplayName("获取Expiration配置")
    void testGetExpiration() {
        Long expiration = jwtUtil.getExpiration();

        assertEquals(TEST_EXPIRATION, expiration);
    }

    @Test
    @DisplayName("生成Token - 包含正确的Claims")
    void testGenerateTokenContainsCorrectClaims() {
        Long userId = 123L;
        String username = "testuser123";

        String token = jwtUtil.generateToken(userId, username);
        Claims claims = jwtUtil.parseToken(token);

        assertNotNull(claims);
        assertEquals(userId, claims.get("userId", Long.class));
        assertEquals(username, claims.get("username", String.class));
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    @DisplayName("Token过期时间正确")
    void testTokenExpirationTime() {
        String token = jwtUtil.generateToken(1L, "testuser");
        Claims claims = jwtUtil.parseToken(token);

        assertNotNull(claims);
        long issuedAt = claims.getIssuedAt().getTime();
        long expiration = claims.getExpiration().getTime();

        assertEquals(TEST_EXPIRATION, expiration - issuedAt, 1000L);
    }

    @Test
    @DisplayName("解析Token - 空字符串返回null")
    void testParseTokenEmptyString() {
        Claims claims = jwtUtil.parseToken("");

        assertNull(claims);
    }

    @Test
    @DisplayName("验证Token - 空字符串返回false")
    void testValidateTokenEmptyString() {
        boolean isValid = jwtUtil.validateToken("");

        assertFalse(isValid);
    }
}
