import apiClient from './client';
import type { ApiResponse, PageResponse } from '../types/api';
import type {
  AiNewsCreateRequest,
  AiNewsListParams,
  AiNewsResponse,
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
