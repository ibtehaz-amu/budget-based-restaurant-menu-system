package com.restaurant.backend.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
public class JwtUtil {

    @Value("${jwt.secret:restaurantBudgetMenuSecretKeyForJWTTokenGeneration2024With32Chars}") // ✅ 32 chars
    private String SECRET_KEY;

    @Value("${jwt.expiration:2592000000}") // Default 24 hours
    private long EXPIRATION_TIME;

    // ✅ FIXED - Proper key generation with exact 32 characters
    private Key getSigningKey() {
        // Ensure exactly 32 characters for HS256
        String secret = SECRET_KEY;
        if (secret.length() != 32) {
            // Pad or truncate to exactly 32 characters
            if (secret.length() < 32) {
                secret = String.format("%-32s", secret).replace(' ', 'X');
            } else {
                secret = secret.substring(0, 32);
            }
            System.out.println("🔑 Adjusted secret to 32 chars: " + secret);
        }
        byte[] keyBytes = secret.getBytes();
        return Keys.hmacShaKeyFor(keyBytes);
    }

    /**
     * GENERATE TOKEN - Basic version with email and role
     */
    public String generateToken(String email, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", role);
        return createToken(claims, email);
    }

    /**
     * CREATE JWT TOKEN - Core token creation method
     */
    private String createToken(Map<String, Object> claims, String subject) {
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_TIME))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * VALIDATE TOKEN - Detailed validation with logging
     */
    public Boolean validateToken(String token) {
        try {
            System.out.println("🔐 Validating token: " + token.substring(0, Math.min(20, token.length())) + "...");

            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);

            System.out.println("✅ Token validation SUCCESS");
            return true;

        } catch (ExpiredJwtException e) {
            System.out.println("❌ Token EXPIRED: " + e.getMessage());
            return false;
        } catch (MalformedJwtException e) {
            System.out.println("❌ Token MALFORMED: " + e.getMessage());
            return false;
        } catch (SignatureException e) {
            System.out.println("❌ Token SIGNATURE INVALID: " + e.getMessage());
            return false;
        } catch (JwtException | IllegalArgumentException e) {
            System.out.println("❌ JWT validation error: " + e.getMessage());
            return false;
        }
    }

    /**
     * EXTRACT USERNAME (email) from token
     */
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * EXTRACT ROLE from token
     */
    public String extractRole(String token) {
        return extractClaim(token, claims -> claims.get("role", String.class));
    }

    /**
     * EXTRACT EXPIRATION DATE from token
     */
    public Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * EXTRACT SPECIFIC CLAIM from token
     */
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * EXTRACT ALL CLAIMS from token
     */
    private Claims extractAllClaims(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (Exception e) {
            System.out.println("❌ Error extracting claims: " + e.getMessage());
            throw e;
        }
    }

    /**
     * CHECK IF TOKEN IS EXPIRED
     */
    public Boolean isTokenExpired(String token) {
        return extractExpiration(token).before(new Date());
    }
}