package com.songzinzza.moaai.domain.user.dto;

import com.songzinzza.moaai.domain.user.entity.Role;
import com.songzinzza.moaai.domain.user.entity.SocialProvider;
import com.songzinzza.moaai.domain.user.entity.User;

public record UserResponse(
    Long id,
    String email,
    String nickname,
    SocialProvider provider,
    Role role,
    String profileImageUrl) {
  public static UserResponse from(User user) {
    return new UserResponse(
        user.getId(),
        user.getEmail(),
        user.getNickname(),
        user.getProvider(),
        user.getRole(),
        user.getProfileImageUrl());
  }
}
