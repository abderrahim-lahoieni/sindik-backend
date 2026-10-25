package com.sindikbackend.auth.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Component
public class JwtUtils {

    private static final String TOKEN_TYPE_CLAIM = "tokenType";
    private static final String ACCESS_TOKEN = "access";
    private static final String REFRESH_TOKEN = "refresh";

    @Value("${security.jwt.secret}")
    private String secret;

    @Value("${security.jwt.expiration:86400000}")
    private long jwtExpirationMs;

    @Value("${security.jwt.refresh-expiration:604800000}")
    private long refreshExpirationMs;

    @Value("${security.jwt.clock-skew-seconds:60}")
    private long clockSkewSeconds;

    public String generateToken(UserDetails user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("authorities", extractAuthorities(user));
        claims.put(TOKEN_TYPE_CLAIM, ACCESS_TOKEN);
        return buildToken(claims, user.getUsername(), jwtExpirationMs);
    }

    public String generateRefreshToken(UserDetails user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(TOKEN_TYPE_CLAIM, REFRESH_TOKEN);
        return buildToken(claims, user.getUsername(), refreshExpirationMs);
    }

    public boolean isValidAccessToken(String token, UserDetails user) {
        return isValid(token, user, ACCESS_TOKEN);
    }

    public boolean isValidRefreshToken(String token, UserDetails user) {
        return isValid(token, user, REFRESH_TOKEN);
    }

    private boolean isValid(String token, UserDetails user, String expectedType) {
        try {
            Claims claims = parseClaims(token);
            Date expiration = claims.getExpiration();
            return expectedType.equals(claims.get(TOKEN_TYPE_CLAIM, String.class))
                    && user.getUsername().equals(claims.getSubject())
                    && expiration != null && expiration.after(new Date());
        } catch (ExpiredJwtException e) {
            log.warn("JWT token is expired: {}", e.getMessage());
            return false;
        } catch (UnsupportedJwtException e) {
            log.warn("JWT token is unsupported: {}", e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
            return false;
        } catch (SignatureException e) {
            log.warn("JWT signature validation failed: {}", e.getMessage());
            return false;
        } catch (IllegalArgumentException e) {
            log.warn("JWT claims string is empty: {}", e.getMessage());
            return false;
        }
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public List<String> extractAuthorities(String token) {
        Object val = parseClaims(token).get("authorities");
        if (val instanceof List<?> list) {
            return list.stream().map(Object::toString).collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    public String extractJti(String token) {
        return parseClaims(token).getId();
    }

    private String buildToken(Map<String, Object> claims, String subject, long expMs) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expMs);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setId(UUID.randomUUID().toString())
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(signingKey(), SignatureAlgorithm.HS512)
                .compact();
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey())
                .setAllowedClockSkewSeconds(clockSkewSeconds)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public long getExpirationMs() {
        return jwtExpirationMs;
    }

    public long getRefreshExpirationMs() {
        return refreshExpirationMs;
    }

    private Key signingKey() {
        byte[] keyBytes = isBase64(secret)
                ? Decoders.BASE64.decode(secret)
                : secret.getBytes(StandardCharsets.UTF_8);

        // HS512 = minimum 64 bytes
        if (keyBytes.length < 64) {
            throw new IllegalStateException(
                    "JWT secret must be at least 64 bytes (512 bits) for HS512. Current: " + keyBytes.length
            );
        }

        return Keys.hmacShaKeyFor(keyBytes);
    }


    private boolean isBase64(String s) {
        try {
            Decoders.BASE64.decode(s);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private List<String> extractAuthorities(UserDetails user) {
        return user.getAuthorities()
                .stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());
    }
}
