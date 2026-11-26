export type SocialProvider = 'GOOGLE' | 'KAKAO' | 'NAVER';

export type Role = 'USER' | 'ADMIN';

export interface UserResponse {
  id: number;
  email: string;
  nickname: string;
  provider: SocialProvider;
  role: Role;
}

export interface TokenResponse {
  accessToken: string;
  refreshToken: string;
}

export interface TokenReissueRequest {
  refreshToken: string;
}
