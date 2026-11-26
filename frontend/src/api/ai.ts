import apiClient from './client';
import type { ApiResponse } from '../types/api';
import type { PromptRefineRequest, PromptRefineResponse } from '../types/ai';

export async function getRemainingRefineCount(): Promise<number> {
  const { data } = await apiClient.get<ApiResponse<number>>(
    '/ai/prompts/refine/remaining',
  );
  return data.data;
}

export async function refinePrompt(
  payload: PromptRefineRequest,
): Promise<PromptRefineResponse> {
  const { data } = await apiClient.post<ApiResponse<PromptRefineResponse>>(
    '/ai/prompts/refine',
    payload,
  );
  return data.data;
}
