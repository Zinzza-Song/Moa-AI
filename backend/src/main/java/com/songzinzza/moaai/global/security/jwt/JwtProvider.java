package com.songzinzza.moaai.global.security.jwt;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;

@Component
public class JwtProvider {
  @Value("${jwt.secret}")
  private String secret;

  @Value("${jwt.access-expiration}")
  private long accessExpiration;

  @Value("${jwt.refresh-expiration}")
  private long refreshExpiration;

  private SecretKey key;

  @PostConstruct
  public void init() {
    this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
  }

  public String createAccessToken(Long userId, String role) {
    return createToken(userId, role, accessExpiration);
  }

  public String createRefreshToken(Long userId, String role) {
    return createToken(userId, role, refreshExpiration);
  }

  private String createToken(Long userId, String role, long expirationMillis) {
    Date now = new Date();

    return Jwts.builder()
        .subject(String.valueOf(userId))
        .claim("role", role)
        .issuedAt(now)
        .expiration(new Date(now.getTime() + expirationMillis))
        .signWith(key)
        .compact();
  }

  public boolean validateToken(String token) {
    try {
      parseClaims(token);
      return true;
    } catch (JwtException | IllegalArgumentException e) {
      return false;
    }
  }

  public Long getUserId(String token) {
    return Long.parseLong(parseClaims(token).getSubject());
  }

  public String getRole(String token) {
    return parseClaims(token).get("role", String.class);
  }

  public long getRefreshExpiration() {
    return refreshExpiration;
  }

  private Claims parseClaims(String token) {
    return Jwts.parser()
        .verifyWith(key)
        .build()
        .parseSignedClaims(token)
        .getPayload();
  }
}
