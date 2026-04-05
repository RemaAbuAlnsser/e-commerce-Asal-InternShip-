package com.asal.ecommerce.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {

    private JwtUtil jwtUtil;
    private final String testSecret = "mySecretKey123456789012345678901234567890";
    private final Long testExpiration = 86400000L; // 24 hours

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil();
        ReflectionTestUtils.setField(jwtUtil, "secret", testSecret);
        ReflectionTestUtils.setField(jwtUtil, "expiration", testExpiration);
    }

    @Test
    void shouldGenerateValidToken_whenValidUserData() {
        // Given
        Long userId = 1L;
        String email = "admin@example.com";
        String role = "ADMIN";

        // When
        String token = jwtUtil.generateToken(userId, email, role);

        // Then
        assertNotNull(token);
        assertTrue(token.length() > 0);
        assertTrue(token.contains("."));
    }

    @Test
    void shouldExtractEmail_whenValidToken() {
        // Given
        String email = "admin@example.com";
        String token = jwtUtil.generateToken(1L, email, "ADMIN");

        // When
        String extractedEmail = jwtUtil.getEmailFromToken(token);

        // Then
        assertEquals(email, extractedEmail);
    }

    @Test
    void shouldExtractUserId_whenValidToken() {
        // Given
        Long userId = 1L;
        String token = jwtUtil.generateToken(userId, "admin@example.com", "ADMIN");

        // When
        Long extractedUserId = jwtUtil.getUserIdFromToken(token);

        // Then
        assertEquals(userId, extractedUserId);
    }

    @Test
    void shouldExtractRole_whenValidToken() {
        // Given
        String role = "ADMIN";
        String token = jwtUtil.generateToken(1L, "admin@example.com", role);

        // When
        String extractedRole = jwtUtil.getRoleFromToken(token);

        // Then
        assertEquals(role, extractedRole);
    }

    @Test
    void shouldReturnTrue_whenTokenIsValid() {
        // Given
        String email = "admin@example.com";
        String token = jwtUtil.generateToken(1L, email, "ADMIN");

        // When
        Boolean isValid = jwtUtil.validateToken(token, email);

        // Then
        assertTrue(isValid);
    }

    @Test
    void shouldReturnFalse_whenTokenIsValidatedWithWrongEmail() {
        // Given
        String token = jwtUtil.generateToken(1L, "admin@example.com", "ADMIN");

        // When
        Boolean isValid = jwtUtil.validateToken(token, "wrong@example.com");

        // Then
        assertFalse(isValid);
    }

    @Test
    void shouldReturnTrue_whenValidateTokenWithoutEmail() {
        // Given
        String token = jwtUtil.generateToken(1L, "admin@example.com", "ADMIN");

        // When
        Boolean isValid = jwtUtil.validateToken(token);

        // Then
        assertTrue(isValid);
    }

    @Test
    void shouldReturnFalse_whenValidateInvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";

        // When
        Boolean isValid = jwtUtil.validateToken(invalidToken);

        // Then
        assertFalse(isValid);
    }

    @Test
    void shouldReturnFalse_whenTokenIsNotExpired() {
        // Given
        String token = jwtUtil.generateToken(1L, "admin@example.com", "ADMIN");

        // When
        Date expirationDate = jwtUtil.getExpirationDateFromToken(token);
        boolean isExpired = expirationDate.before(new Date());

        // Then
        assertFalse(isExpired);
        assertTrue(expirationDate.after(new Date()));
    }

    @Test
    void shouldReturnTrue_whenTokenIsExpired() {
        // Given - Create a token with very short expiration
        ReflectionTestUtils.setField(jwtUtil, "expiration", 1L); // 1 millisecond
        String token = jwtUtil.generateToken(1L, "admin@example.com", "ADMIN");

        // Wait for token to expire
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // When - This should catch ExpiredJwtException and return false for validateToken
        Boolean isValid = jwtUtil.validateToken(token);

        // Then - Expired token should be invalid
        assertFalse(isValid);
    }

    @Test
    void shouldReturnExpirationTime_whenGetExpirationTime() {
        // When
        Long expirationTime = jwtUtil.getExpirationTime();

        // Then
        assertEquals(testExpiration / 1000, expirationTime);
    }

    @Test
    void shouldExtractExpirationDate_whenValidToken() {
        // Given
        String token = jwtUtil.generateToken(1L, "admin@example.com", "ADMIN");

        // When
        Date expirationDate = jwtUtil.getExpirationDateFromToken(token);

        // Then
        assertNotNull(expirationDate);
        assertTrue(expirationDate.after(new Date()));
    }

    @Test
    void shouldThrowException_whenExtractingFromInvalidToken() {
        // Given
        String invalidToken = "invalid.token.here";

        // When & Then
        assertThrows(Exception.class, () -> {
            jwtUtil.getEmailFromToken(invalidToken);
        });
    }

    @Test
    void shouldGenerateDifferentTokens_forDifferentUsers() {
        // Given
        String token1 = jwtUtil.generateToken(1L, "user1@example.com", "USER");
        String token2 = jwtUtil.generateToken(2L, "user2@example.com", "ADMIN");

        // When & Then
        assertNotEquals(token1, token2);
        assertEquals("user1@example.com", jwtUtil.getEmailFromToken(token1));
        assertEquals("user2@example.com", jwtUtil.getEmailFromToken(token2));
        assertEquals(Long.valueOf(1L), jwtUtil.getUserIdFromToken(token1));
        assertEquals(Long.valueOf(2L), jwtUtil.getUserIdFromToken(token2));
    }
}
