import apiClient from './client';
import type { ApiResponse, PageResponse } from '../types/api';
import type {
  PromptCreateRequest,
  PromptListParams,
  PromptResponse,
  PromptToggleResponse,
  PromptUpdateRequest,
} from '../types/prompt';

export async function getPrompts(
  params: PromptListParams = {},
): Promise<PageResponse<PromptResponse>> {
  const { data } = await apiClient.get<
    ApiResponse<PageResponse<PromptResponse>>
  >('/prompts', {
    params: {
      ...(params.category ? { category: params.category } : {}),
      ...(params.aiModel ? { aiModel: params.aiModel } : {}),
      ...(params.keyword?.trim() ? { keyword: params.keyword.trim() } : {}),
      page: params.page ?? 0,
      size: params.size ?? 12,
      sortBy: params.sortBy ?? 'createdAt',
    },
  });
  return data.data;
}

export async function getPopularPrompts(
  limit = 5,
): Promise<PromptResponse[]> {
  const { data } = await apiClient.get<ApiResponse<PromptResponse[]>>(
    '/prompts/popular',
    {
      params: { limit },
    },
  );
  return data.data;
}

export async function getPromptDetail(
  promptId: number,
): Promise<PromptResponse> {
  const { data } = await apiClient.get<ApiResponse<PromptResponse>>(
    `/prompts/${promptId}`,
  );
  return data.data;
}

export async function createPrompt(
  payload: PromptCreateRequest,
): Promise<PromptResponse> {
  const { data } = await apiClient.post<ApiResponse<PromptResponse>>(
    '/prompts',
    payload,
  );
  return data.data;
}

export async function updatePrompt(
  promptId: number,
  payload: PromptUpdateRequest,
): Promise<PromptResponse> {
  const { data } = await apiClient.put<ApiResponse<PromptResponse>>(
    `/prompts/${promptId}`,
    payload,
  );
  return data.data;
}

export async function deletePrompt(promptId: number): Promise<void> {
  await apiClient.delete<ApiResponse<void>>(`/prompts/${promptId}`);
}

export async function togglePromptLike(
  promptId: number,
): Promise<PromptToggleResponse> {
  const { data } = await apiClient.post<ApiResponse<PromptToggleResponse>>(
    `/prompts/${promptId}/like`,
  );
  return data.data;
}

export async function togglePromptBookmark(
  promptId: number,
): Promise<PromptToggleResponse> {
  const { data } = await apiClient.post<ApiResponse<PromptToggleResponse>>(
    `/prompts/${promptId}/bookmark`,
  );
  return data.data;
}

export async function increasePromptCopyCount(
  promptId: number,
): Promise<number> {
  const { data } = await apiClient.post<ApiResponse<number>>(
    `/prompts/${promptId}/copy`,
  );
  return data.data;
}
