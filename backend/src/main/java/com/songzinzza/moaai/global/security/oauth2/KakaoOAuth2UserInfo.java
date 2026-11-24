package com.songzinzza.moaai.global.security.oauth2;

import java.util.Map;

import com.songzinzza.moaai.domain.user.entity.SocialProvider;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class KakaoOAuth2UserInfo implements OAuth2UserInfo {
  private final Map<String, Object> attributes;

  @Override
  public SocialProvider getProvider() {
    return SocialProvider.KAKAO;
  }

  @Override
  public String getProviderId() {
    Object id = attributes.get("id");
    if (id == null) {
      throw new IllegalStateException("카카오 응답에서 id를 찾을 수 없습니다: " + attributes);
    }
    return String.valueOf(id);
  }

  @Override
  public String getEmail() {
    if (attributes.get("kakao_account") instanceof Map<?, ?> account) {
      Object email = account.get("email");
      return email != null ? String.valueOf(email) : null;
    }
    return null;
  }

  @Override
  public String getNickname() {
    if (attributes.get("kakao_account") instanceof Map<?, ?> account
        && account.get("profile") instanceof Map<?, ?> profile) {
      Object nickname = profile.get("nickname");
      if (nickname != null) {
        return String.valueOf(nickname);
      }
    }
    String id = getProviderId();
    return "카카오유저_" + id.substring(0, Math.min(6, id.length()));
  }
}
