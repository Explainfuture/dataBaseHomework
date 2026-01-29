package com.hnu.campus.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {
    private final SecretKey secretKey;
    private final long accessExpireSeconds;
    private final String issuer;

    public JwtUtil(@Value("${jwt.secret}") String secret,
                   @Value("${jwt.access-expire-seconds:1800}") long accessExpireSeconds,
                   @Value("${jwt.issuer:hnu-campus}") String issuer) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessExpireSeconds = accessExpireSeconds;
        this.issuer = issuer;
    }

    public String generateToken(Long userId, String role, Long tokenVersion) {
        Date now = new Date();
        Date expireAt = new Date(now.getTime() + accessExpireSeconds * 1000);
        return Jwts.builder()
                .subject(String.valueOf(userId))
                .issuer(issuer)
                .issuedAt(now)
                .expiration(expireAt)
                .claim("role", role)
                .claim("ver", tokenVersion)
                .signWith(secretKey)
                .compact();
    }

    public Claims parseToken(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public Long getUserId(Claims claims) {
        return Long.valueOf(claims.getSubject());
    }

    public String getRole(Claims claims) {
        Object role = claims.get("role");
        return role == null ? null : role.toString();
    }

    public Long getTokenVersion(Claims claims) {
        Object version = claims.get("ver");
        if (version == null) {
            return null;
        }
        if (version instanceof Number number) {
            return number.longValue();
        }
        try {
            return Long.valueOf(version.toString());
        } catch (NumberFormatException ex) {
            return null;
        }
    }

    public long getAccessExpireSeconds() {
        return accessExpireSeconds;
    }
}
