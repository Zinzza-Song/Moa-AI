package com.songzinzza.moaai.domain.auth.dto;

public record TokenResponse(
    String accessToken,
    String refreshToken) {
}
