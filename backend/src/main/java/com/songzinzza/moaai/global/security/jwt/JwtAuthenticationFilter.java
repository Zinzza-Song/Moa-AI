package com.songzinzza.moaai.global.security.jwt;

import java.io.IOException;
import java.util.List;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
  private final JwtProvider jwtProvider;

  @Override
  protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res,
      FilterChain filterChain) throws ServletException, IOException {
    String token = resolveToken(req);

    if (StringUtils.hasText(token) && jwtProvider.validateToken(token)) {
      Long userId = jwtProvider.getUserId(token);
      String role = jwtProvider.getRole(token);

      UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userId, null,
          List.of(new SimpleGrantedAuthority(role)));

      SecurityContextHolder.getContext().setAuthentication(authentication);
    }

    filterChain.doFilter(req, res);
  }

  private String resolveToken(HttpServletRequest req) {
    String bearer = req.getHeader("Authorization");

    if (StringUtils.hasText(bearer) && bearer.startsWith("Bearer "))
      return bearer.substring(7);

    return null;
  }
}
