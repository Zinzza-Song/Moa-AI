package com.songzinzza.moaai.global.security.oauth2;

import java.util.Map;

import com.songzinzza.moaai.domain.user.entity.SocialProvider;

public class OAuth2UserInfoFactory {
  public static OAuth2UserInfo create(SocialProvider provider, Map<String, Object> attributes) {
    return switch (provider) {
      case GOOGLE -> new GoogleOAuth2UserInfo(attributes);
      case KAKAO -> new KakaoOAuth2UserInfo(attributes);
      case NAVER -> new NaverOAuth2UserInfo(attributes);
    };
  }
}
