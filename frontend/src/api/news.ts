import apiClient from './client';
import type { ApiResponse, PageResponse } from '../types/api';
import type {
  AiNewsCollectResponse,
  AiNewsCreateRequest,
  AiNewsListParams,
  AiNewsResponse,
  AiVideoResponse,
} from '../types/news';

export async function getNewsList(
  params: AiNewsListParams = {},
): Promise<PageResponse<AiNewsResponse>> {
  const { data } = await apiClient.get<
    ApiResponse<PageResponse<AiNewsResponse>>
  >('/news', {
    params: {
      ...(params.keyword?.trim() ? { keyword: params.keyword.trim() } : {}),
      ...(params.tag?.trim() ? { tag: params.tag.trim() } : {}),
      page: params.page ?? 0,
      size: params.size ?? 10,
    },
  });
  return data.data;
}

export async function getNewsDetail(newsId: number): Promise<AiNewsResponse> {
  const { data } = await apiClient.get<ApiResponse<AiNewsResponse>>(
    `/news/${newsId}`,
  );
  return data.data;
}

export async function createAdminNews(
  payload: AiNewsCreateRequest,
): Promise<AiNewsResponse> {
  const { data } = await apiClient.post<ApiResponse<AiNewsResponse>>(
    '/admin/news',
    payload,
  );
  return data.data;
}

export async function deleteAdminNews(newsId: number): Promise<void> {
  await apiClient.delete<ApiResponse<void>>(`/admin/news/${newsId}`);
}

export async function collectAiNews(): Promise<AiNewsCollectResponse> {
  const { data } = await apiClient.post<ApiResponse<AiNewsCollectResponse>>(
    '/admin/news/collect',
    undefined,
    { timeout: 60_000 },
  );
  return data.data;
}

export async function getNewsVideos(limit = 12): Promise<AiVideoResponse[]> {
  const { data } = await apiClient.get<ApiResponse<AiVideoResponse[]>>(
    '/news/videos',
    {
      params: { limit },
    },
  );
  return data.data;
}

export async function collectAiVideos(): Promise<string> {
  const { data } = await apiClient.post<ApiResponse<string>>(
    '/admin/news/videos/collect',
    undefined,
    { timeout: 60_000 },
  );
  return data.data;
}
