package com.sindikbackend.auth.security.jwt;

import com.sindikbackend.auth.entities.Role;
import com.sindikbackend.auth.entities.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class JwtUtilsSecurityTest {
    private JwtUtils jwtUtils;
    private User user;

    @BeforeEach
    void setUp() {
        jwtUtils = new JwtUtils();
        ReflectionTestUtils.setField(jwtUtils, "secret", Base64.getEncoder().encodeToString(new byte[64]));
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 60_000L);
        ReflectionTestUtils.setField(jwtUtils, "refreshExpirationMs", 120_000L);
        ReflectionTestUtils.setField(jwtUtils, "clockSkewSeconds", 0L);
        user = User.builder().email("resident@example.com").role(Role.RESIDENT).build();
    }

    @Test
    void accessAndRefreshTokensHaveDistinctTypesAndValidSignatures() {
        String access = jwtUtils.generateToken(user);
        String refresh = jwtUtils.generateRefreshToken(user);

        assertTrue(jwtUtils.isValidAccessToken(access, user));
        assertFalse(jwtUtils.isValidRefreshToken(access, user));
        assertTrue(jwtUtils.isValidRefreshToken(refresh, user));
        assertFalse(jwtUtils.isValidAccessToken(refresh, user));
        assertFalse(jwtUtils.isValidAccessToken(access + "x", user));
    }

    @Test
    void tokensForAnotherUserAreRejected() {
        String token = jwtUtils.generateToken(user);
        User other = User.builder().email("other@example.com").role(Role.RESIDENT).build();
        assertFalse(jwtUtils.isValidAccessToken(token, other));
    }

    @Test
    void expiredTokensAreRejected() throws InterruptedException {
        ReflectionTestUtils.setField(jwtUtils, "jwtExpirationMs", 1L);
        ReflectionTestUtils.setField(jwtUtils, "refreshExpirationMs", 1L);
        String access = jwtUtils.generateToken(user);
        String refresh = jwtUtils.generateRefreshToken(user);
        Thread.sleep(10);

        assertFalse(jwtUtils.isValidAccessToken(access, user));
        assertFalse(jwtUtils.isValidRefreshToken(refresh, user));
    }
}
