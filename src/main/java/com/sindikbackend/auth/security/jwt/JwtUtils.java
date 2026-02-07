
package com.sindikbackend.auth.security.jwt;

import io.jsonwebtoken.*;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.GrantedAuthority;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.*;

@Component
public class JwtUtils {

    @Value("${security.jwt.secret}")
    private String secret;

    @Value("${security.jwt.expiration:86400000}")
    private long jwtExpirationMs;

    @Value("${security.jwt.clock-skew-seconds:60}")
    private long clockSkewSeconds;

    // ============================
    // PUBLIC
    // ============================

    public String generateToken(UserDetails user) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("roles", extractAuthorities(user));
        return buildToken(claims, user.getUsername(), jwtExpirationMs);
    }

    public boolean isValid(String token, UserDetails user) {
        try {
            String username = extractUsername(token);
            return username.equals(user.getUsername()) && !isExpired(token);
        } catch (Exception e) {
            return false;
        }
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public List<String> extractRoles(String token) {
        Object val = parseClaims(token).get("roles");
        if (val instanceof List<?> list) {
            return list.stream().map(Object::toString).toList();
        }
        return Collections.emptyList();
    }

    // ============================
    // INTERNALS
    // ============================

    private String buildToken(Map<String, Object> claims, String subject, long expMs) {

        Date now = new Date();
        Date expiry = new Date(now.getTime() + expMs);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiry)
                .signWith(signingKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(signingKey())
                .setAllowedClockSkewSeconds(clockSkewSeconds)
                .build()
                .parseClaimsJws(token)   // ✅ la bonne méthode pour jjwt 0.11.5
                .getBody();
    }

    private boolean isExpired(String token) {
        return parseClaims(token).getExpiration().before(new Date());
    }

    private Key signingKey() {
        byte[] keyBytes = isBase64(secret)
                ? Decoders.BASE64.decode(secret)
                : secret.getBytes(StandardCharsets.UTF_8);

        if (keyBytes.length < 32) {
            throw new IllegalStateException("JWT secret must be at least 32 bytes (256 bits).");
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
                .toList();
    }
}
