package com.songzinzza.moaai.global.security.oauth2;

import java.util.Map;

import com.songzinzza.moaai.domain.user.entity.SocialProvider;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class GoogleOAuth2UserInfo implements OAuth2UserInfo {
  private final Map<String, Object> attributes;

  @Override
  public SocialProvider getProvider() {
    return SocialProvider.GOOGLE;
  }

  @Override
  public String getProviderId() {
    Object id = attributes.get("sub");
    if (id == null) {
      throw new IllegalStateException("구글 응답에서 sub(id)를 찾을 수 없습니다: " + attributes);
    }
    return String.valueOf(id);
  }

  @Override
  public String getEmail() {
    Object email = attributes.get("email");
    return email != null ? String.valueOf(email) : null;
  }

  @Override
  public String getNickname() {
    Object name = attributes.get("name");
    if (name != null) {
      return String.valueOf(name);
    }
    String id = getProviderId();
    return "구글유저_" + id.substring(0, Math.min(6, id.length()));
  }
}
