import axios, { AxiosError, type InternalAxiosRequestConfig } from 'axios';
import type { ApiResponse } from '../types/api';
import type { TokenReissueRequest, TokenResponse } from '../types/auth';

const ACCESS_TOKEN_KEY = 'accessToken';
const REFRESH_TOKEN_KEY = 'refreshToken';

interface RetryableRequestConfig extends InternalAxiosRequestConfig {
  _retry?: boolean;
}

export const apiClient = axios.create({
  baseURL: '/api',
  headers: {
    'Content-Type': 'application/json',
  },
});

let refreshPromise: Promise<TokenResponse> | null = null;

function clearTokensAndRedirectToLogin(): void {
  localStorage.removeItem(ACCESS_TOKEN_KEY);
  localStorage.removeItem(REFRESH_TOKEN_KEY);
  if (window.location.pathname !== '/login') {
    window.location.href = '/login';
  }
}

async function requestTokenReissue(refreshToken: string): Promise<TokenResponse> {
  const payload: TokenReissueRequest = { refreshToken };
  const { data } = await axios.post<ApiResponse<TokenResponse>>(
    '/api/auth/reissue',
    payload,
    {
      headers: {
        'Content-Type': 'application/json',
      },
    },
  );

  if (!data.success || !data.data) {
    throw new Error(data.message || '토큰 재발급에 실패했습니다.');
  }

  localStorage.setItem(ACCESS_TOKEN_KEY, data.data.accessToken);
  localStorage.setItem(REFRESH_TOKEN_KEY, data.data.refreshToken);
  return data.data;
}

apiClient.interceptors.request.use((config) => {
  const accessToken = localStorage.getItem(ACCESS_TOKEN_KEY);
  if (accessToken) {
    config.headers.Authorization = `Bearer ${accessToken}`;
  }
  return config;
});

apiClient.interceptors.response.use(
  (response) => response,
  async (error: AxiosError) => {
    const originalRequest = error.config as RetryableRequestConfig | undefined;

    if (
      error.response?.status !== 401 ||
      !originalRequest ||
      originalRequest._retry ||
      originalRequest.url?.includes('/auth/reissue')
    ) {
      return Promise.reject(error);
    }

    const refreshToken = localStorage.getItem(REFRESH_TOKEN_KEY);
    if (!refreshToken) {
      clearTokensAndRedirectToLogin();
      return Promise.reject(error);
    }

    originalRequest._retry = true;

    try {
      if (!refreshPromise) {
        refreshPromise = requestTokenReissue(refreshToken).finally(() => {
          refreshPromise = null;
        });
      }

      const tokens = await refreshPromise;
      originalRequest.headers.Authorization = `Bearer ${tokens.accessToken}`;
      return apiClient(originalRequest);
    } catch (reissueError) {
      clearTokensAndRedirectToLogin();
      return Promise.reject(reissueError);
    }
  },
);

export default apiClient;
