import apiClient from './client';
import type { ApiResponse } from '../types/api';
import type {
  TokenReissueRequest,
  TokenResponse,
  UserResponse,
} from '../types/auth';

export async function getMyInfo(): Promise<UserResponse> {
  const { data } = await apiClient.get<ApiResponse<UserResponse>>('/users/me');
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
