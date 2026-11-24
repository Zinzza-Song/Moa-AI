package com.songzinzza.moaai.global.security.oauth2;

import java.util.Map;

import com.songzinzza.moaai.domain.user.entity.SocialProvider;

@SuppressWarnings("unchecked")
public class NaverOAuth2UserInfo implements OAuth2UserInfo {
  private final Map<String, Object> response;

  public NaverOAuth2UserInfo(Map<String, Object> attributes) {
    Object res = attributes.get("response");
    this.response = (res instanceof Map) ? (Map<String, Object>) res : attributes;
  }

  @Override
  public SocialProvider getProvider() {
    return SocialProvider.NAVER;
  }

  @Override
  public String getProviderId() {
    Object id = response.get("id");
    if (id == null) {
      throw new IllegalStateException("네이버 응답에서 id를 찾을 수 없습니다: " + response);
    }
    return String.valueOf(id);
  }

  @Override
  public String getEmail() {
    Object email = response.get("email");
    return email != null ? String.valueOf(email) : null;
  }

  @Override
  public String getNickname() {
    Object nickname = response.get("nickname");
    if (nickname != null) {
      return String.valueOf(nickname);
    }
    Object name = response.get("name");
    return name != null ? String.valueOf(name) : "네이버유저";
  }
}
