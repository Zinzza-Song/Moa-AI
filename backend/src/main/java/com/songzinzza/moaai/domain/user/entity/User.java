package com.songzinzza.moaai.domain.user.entity;

import com.songzinzza.moaai.global.common.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "users", uniqueConstraints = {
    @UniqueConstraint(name = "uk_user_providder_id", columnNames = { "provider", "provider_id" })
})
public class User extends BaseTimeEntity {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private Long id;

  @Column(nullable = false, length = 100)
  private String email;

  @Column(nullable = false, length = 50)
  private String nickname;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private SocialProvider provider;

  @Column(name = "provider_id", nullable = false, length = 100)
  private String providerId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  private Role role;

  @Builder
  public User(String email, String nickname, SocialProvider provider, String providerId, Role role) {
    this.email = email;
    this.nickname = nickname;
    this.provider = provider;
    this.providerId = providerId;
    this.role = role != null ? role : Role.USER;
  }

  public void updateProfile(String nickname, String email) {
    if (nickname != null && !email.isBlank())
      this.nickname = nickname;

    if (email != null && !email.isBlank())
      this.email = email;
  }
}
