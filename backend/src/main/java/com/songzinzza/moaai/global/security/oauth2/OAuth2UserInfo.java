package com.songzinzza.moaai.global.security.oauth2;

import com.songzinzza.moaai.domain.user.entity.SocialProvider;

public interface OAuth2UserInfo {
  SocialProvider getProvider();

  String getProviderId();

  String getEmail();

  String getNickname();
}
