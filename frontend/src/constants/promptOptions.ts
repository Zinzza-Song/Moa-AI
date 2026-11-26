import axios from 'axios';
import type { ApiResponse } from '../types/api';
import type { AiModel, PromptCategory, PromptSortBy } from '../types/prompt';

export const PROMPT_CATEGORIES: { value: PromptCategory; label: string }[] = [
  { value: 'TEXT', label: '텍스트' },
  { value: 'DEV', label: '개발' },
  { value: 'IMAGE', label: '이미지' },
  { value: 'VIDEO', label: '영상' },
  { value: 'MARKETING', label: '마케팅' },
  { value: 'ETC', label: '기타' },
];

export const AI_MODELS: { value: AiModel; label: string }[] = [
  { value: 'GEMINI', label: 'Gemini' },
  { value: 'CHATGPT', label: 'ChatGPT' },
  { value: 'CLAUDE', label: 'Claude' },
  { value: 'MIDJOURNEY', label: 'Midjourney' },
  { value: 'SORA', label: 'Sora' },
  { value: 'VEO', label: 'Veo' },
  { value: 'ETC', label: '기타' },
];

export const PROMPT_SORT_OPTIONS: { value: PromptSortBy; label: string }[] = [
  { value: 'createdAt', label: '최신순' },
  { value: 'likeCount', label: '좋아요순' },
  { value: 'copyCount', label: '복사순' },
];

export const CATEGORY_LABEL_MAP: Record<PromptCategory, string> =
  Object.fromEntries(
    PROMPT_CATEGORIES.map((item) => [item.value, item.label]),
  ) as Record<PromptCategory, string>;

export const AI_MODEL_LABEL_MAP: Record<AiModel, string> = Object.fromEntries(
  AI_MODELS.map((item) => [item.value, item.label]),
) as Record<AiModel, string>;

export function formatDateTime(isoString: string): string {
  if (!isoString) return '';
  const date = new Date(isoString);
  if (Number.isNaN(date.getTime())) return isoString;
  return date.toLocaleDateString('ko-KR', {
    year: 'numeric',
    month: 'short',
    day: 'numeric',
  });
}

export function extractApiError(
  error: unknown,
  fallbackMessage = '요청 처리 중 오류가 발생했습니다.',
): { status?: number; code?: string; message: string } {
  if (axios.isAxiosError<ApiResponse<unknown>>(error)) {
    const status = error.response?.status;
    const body = error.response?.data;
    return {
      status,
      code: body?.code,
      message: body?.message || fallbackMessage,
    };
  }
  if (error instanceof Error) {
    return { message: error.message };
  }
  return { message: fallbackMessage };
}
