package com.yukeshkumar.user_management_service.security;

import com.yukeshkumar.user_management_service.model.CustomUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilityTest {

    private JwtUtility jwtUtility;

    @BeforeEach
    void setUp() {
        jwtUtility = new JwtUtility("mySecretKeyForTestingPurposesOnly12345678901234567890", 3600000L);
    }

    @Test
    void testGenerateToken() {
        UUID userId = UUID.randomUUID();
        CustomUserDetails userDetails = new CustomUserDetails(
                userId,
                "testuser",
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        String token = jwtUtility.generateToken(userDetails);

        assertNotNull(token);
        assertTrue(token.length() > 0);
    }

    @Test
    void testExtractUsername() {
        UUID userId = UUID.randomUUID();
        CustomUserDetails userDetails = new CustomUserDetails(
                userId,
                "testuser",
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        String token = jwtUtility.generateToken(userDetails);
        String extractedUsername = jwtUtility.extractUsername(token);

        assertEquals(userId.toString(), extractedUsername);
    }

    @Test
    void testExtractUserId() {
        UUID userId = UUID.randomUUID();
        CustomUserDetails userDetails = new CustomUserDetails(
                userId,
                "testuser",
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        String token = jwtUtility.generateToken(userDetails);
        UUID extractedUserId = jwtUtility.extractUserId(token);

        assertEquals(userId, extractedUserId);
    }

    @Test
    void testExtractRole() {
        UUID userId = UUID.randomUUID();
        CustomUserDetails userDetails = new CustomUserDetails(
                userId,
                "testuser",
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        String token = jwtUtility.generateToken(userDetails);
        String extractedRole = jwtUtility.extractRole(token);

        assertEquals("ROLE_USER", extractedRole);
    }

    @Test
    void testIsTokenExpired_ValidToken() {
        UUID userId = UUID.randomUUID();
        CustomUserDetails userDetails = new CustomUserDetails(
                userId,
                "testuser",
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        String token = jwtUtility.generateToken(userDetails);
        boolean isExpired = jwtUtility.isTokenExpired(token);

        assertFalse(isExpired);
    }

    @Test
    void testIsTokenExpired_ExpiredToken() {
        // Create a utility with very short expiration for testing
        JwtUtility shortLivedJwtUtility = new JwtUtility("mySecretKeyForTestingPurposesOnly12345678901234567890", 1L);

        UUID userId = UUID.randomUUID();
        CustomUserDetails userDetails = new CustomUserDetails(
                userId,
                "testuser",
                "password",
                Collections.singletonList(new SimpleGrantedAuthority("ROLE_USER"))
        );

        String token = shortLivedJwtUtility.generateToken(userDetails);

        // Wait for token to expire
        try {
            Thread.sleep(10);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        boolean isExpired = shortLivedJwtUtility.isTokenExpired(token);

        assertTrue(isExpired);
    }

    @Test
    void testIsTokenExpired_InvalidToken() {
        assertThrows(Exception.class, () -> jwtUtility.isTokenExpired("invalid.token.here"));
    }
}
