package com.songzinzza.moaai.global.security.oauth2;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.songzinzza.moaai.domain.user.entity.Role;
import com.songzinzza.moaai.domain.user.entity.SocialProvider;
import com.songzinzza.moaai.domain.user.entity.User;
import com.songzinzza.moaai.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {
  private final UserRepository userRepository;

  @Override
  @Transactional
  public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
    OAuth2User oAuth2User = super.loadUser(userRequest);

    String registrationId = userRequest.getClientRegistration().getRegistrationId();
    SocialProvider provider = SocialProvider.from(registrationId);
    OAuth2UserInfo userInfo = OAuth2UserInfoFactory.create(provider, oAuth2User.getAttributes());

    User user = userRepository.findByProviderAndProviderId(provider, userInfo.getProviderId())
        .map(existing -> {
          existing.updateProfile(userInfo.getNickname(), userInfo.getEmail());

          return existing;
        })
        .orElseGet(() -> userRepository.save(User.builder()
            .email(userInfo.getEmail())
            .nickname(userInfo.getNickname())
            .providerId(userInfo.getProviderId())
            .provider(provider)
            .role(Role.USER)
            .build()));

    return new CustomOAuth2User(user, oAuth2User.getAttributes());
  }
}
