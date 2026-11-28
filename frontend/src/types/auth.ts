export type SocialProvider = 'GOOGLE' | 'KAKAO' | 'NAVER';

export type Role = 'USER' | 'ADMIN';

export interface UserResponse {
  id: number;
  email: string;
  nickname: string;
  profileImageUrl: string | null;
  provider: SocialProvider;
  role: Role;
}

export interface UpdateNicknameRequest {
  nickname: string;
}

export interface UserSummaryResponse {
  writtenPromptCount: number;
  likedPromptCount: number;
  bookmarkedPromptCount: number;
}

export interface TokenResponse {
  accessToken: string;
  refreshToken: string;
}

export interface TokenReissueRequest {
  refreshToken: string;
}
