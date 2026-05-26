package com.example.blog.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.time.Instant;
import java.util.Date;

@Service
public class JwtTokenService {

    private final SecretKey secretKey;
    private final long expireSeconds;

    public JwtTokenService(
            @Value("${JWT_SECRET:change-this-secret-change-this-secret}") String secret,
            @Value("${JWT_EXPIRE_SECONDS:86400}") long expireSeconds
    ) {
        this.secretKey = Keys.hmacShaKeyFor(Decoders.BASE64.decode(toBase64(secret)));
        this.expireSeconds = expireSeconds;
    }

    public String generateToken(AuthenticatedUser user) {
        Instant now = Instant.now();
        return Jwts.builder()
                .subject(user.getUsername())
                .claim("uid", user.getId())
                .claim("role", user.getRole().name())
                .issuedAt(Date.from(now))
                .expiration(Date.from(now.plusSeconds(expireSeconds)))
                .signWith(secretKey)
                .compact();
    }

    public String extractUsername(String token) {
        return parseClaims(token).getSubject();
    }

    public boolean isTokenValid(String token, UserDetails userDetails) {
        Claims claims = parseClaims(token);
        String username = claims.getSubject();
        Date expiration = claims.getExpiration();
        return username.equals(userDetails.getUsername()) && expiration.after(new Date());
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private String toBase64(String secret) {
        return java.util.Base64.getEncoder().encodeToString(secret.getBytes(java.nio.charset.StandardCharsets.UTF_8));
    }
}
