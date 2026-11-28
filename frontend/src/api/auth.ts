import apiClient from './client';
import type { ApiResponse, PageResponse } from '../types/api';
import type {
  TokenReissueRequest,
  TokenResponse,
  UpdateNicknameRequest,
  UserResponse,
  UserSummaryResponse,
} from '../types/auth';
import type { PromptResponse } from '../types/prompt';

export async function getMyInfo(): Promise<UserResponse> {
  const { data } = await apiClient.get<ApiResponse<UserResponse>>('/users/me');
  return data.data;
}

export async function updateMyNickname(
  nickname: string,
): Promise<UserResponse> {
  const payload: UpdateNicknameRequest = { nickname };
  const { data } = await apiClient.patch<ApiResponse<UserResponse>>(
    '/users/me',
    payload,
  );
  return data.data;
}

export async function uploadMyProfileImage(file: File): Promise<UserResponse> {
  const formData = new FormData();
  formData.append('file', file);
  const { data } = await apiClient.post<ApiResponse<UserResponse>>(
    '/users/me/profile-image',
    formData,
    {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    },
  );
  return data.data;
}

export async function deleteMyProfileImage(): Promise<UserResponse> {
  const { data } = await apiClient.delete<ApiResponse<UserResponse>>(
    '/users/me/profile-image',
  );
  return data.data;
}

export async function getMySummary(): Promise<UserSummaryResponse> {
  const { data } = await apiClient.get<ApiResponse<UserSummaryResponse>>(
    '/users/me/summary',
  );
  return data.data;
}

export async function getMyPrompts(
  page = 0,
  size = 12,
): Promise<PageResponse<PromptResponse>> {
  const { data } = await apiClient.get<
    ApiResponse<PageResponse<PromptResponse>>
  >('/users/me/prompts', {
    params: { page, size },
  });
  return data.data;
}

export async function getMyLikedPrompts(
  page = 0,
  size = 12,
): Promise<PageResponse<PromptResponse>> {
  const { data } = await apiClient.get<
    ApiResponse<PageResponse<PromptResponse>>
  >('/users/me/likes', {
    params: { page, size },
  });
  return data.data;
}

export async function getMyBookmarkedPrompts(
  page = 0,
  size = 12,
): Promise<PageResponse<PromptResponse>> {
  const { data } = await apiClient.get<
    ApiResponse<PageResponse<PromptResponse>>
  >('/users/me/bookmarks', {
    params: { page, size },
  });
  return data.data;
}

export async function reissueTokens(
  refreshToken: string,
): Promise<TokenResponse> {
  const payload: TokenReissueRequest = { refreshToken };
  const { data } = await apiClient.post<ApiResponse<TokenResponse>>(
    '/auth/reissue',
    payload,
  );
  return data.data;
}

export async function logoutApi(): Promise<void> {
  await apiClient.post<ApiResponse<void>>('/auth/logout');
}
