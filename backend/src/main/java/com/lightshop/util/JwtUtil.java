package com.lightshop.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class JwtUtil {
    private static final long EXPIRATION_TIME = 7 * 24 * 60 * 60 * 1000; // 7 days
    private static SecretKey secretKey;

    public static void init(String secret) {
        if (secret == null || secret.length() < 32) {
            secret = "lightshop_jwt_secret_key_2024_very_secure_default";
        }
        // Ensure key is at least 256 bits (32 bytes)
        byte[] keyBytes = new byte[32];
        byte[] secretBytes = secret.getBytes(StandardCharsets.UTF_8);
        System.arraycopy(secretBytes, 0, keyBytes, 0, Math.min(secretBytes.length, 32));
        secretKey = Keys.hmacShaKeyFor(keyBytes);
    }

    private static SecretKey getSecretKey() {
        if (secretKey == null) {
            String secret = System.getenv("JWT_SECRET");
            init(secret);
        }
        return secretKey;
    }

    public static String generateToken(int userId, String username, int role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(String.valueOf(userId))
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSecretKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public static Claims parseToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSecretKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean validateToken(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return false;
        }
        return claims.getExpiration().after(new Date());
    }

    public static Integer getUserId(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return null;
        }
        Object userId = claims.get("userId");
        if (userId instanceof Integer) {
            return (Integer) userId;
        } else if (userId instanceof Double) {
            return ((Double) userId).intValue();
        }
        return null;
    }

    public static String getUsername(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return null;
        }
        return (String) claims.get("username");
    }

    public static Integer getRole(String token) {
        Claims claims = parseToken(token);
        if (claims == null) {
            return null;
        }
        Object role = claims.get("role");
        if (role instanceof Integer) {
            return (Integer) role;
        } else if (role instanceof Double) {
            return ((Double) role).intValue();
        }
        return null;
    }

    public static boolean isAdmin(String token) {
        Integer role = getRole(token);
        return role != null && role == 1;
    }
}

