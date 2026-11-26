export type AiModel =
  | 'GEMINI'
  | 'CHATGPT'
  | 'CLAUDE'
  | 'MIDJOURNEY'
  | 'SORA'
  | 'VEO'
  | 'ETC';

export type PromptCategory =
  | 'IMAGE'
  | 'VIDEO'
  | 'TEXT'
  | 'DEV'
  | 'MARKETING'
  | 'ETC';

export type PromptSortBy = 'createdAt' | 'likeCount' | 'copyCount';

export interface PromptListParams {
  category?: PromptCategory;
  aiModel?: AiModel;
  keyword?: string;
  page?: number;
  size?: number;
  sortBy?: PromptSortBy;
}

export interface PromptCreateRequest {
  title: string;
  content: string;
  resultExample?: string;
  category: PromptCategory;
  aiModel: AiModel;
}

export interface PromptUpdateRequest {
  title: string;
  content: string;
  resultExample?: string;
  category: PromptCategory;
  aiModel: AiModel;
}

export interface PromptResponse {
  id: number;
  authorId: number;
  authorNickname: string;
  title: string;
  content: string;
  resultExample: string | null;
  category: PromptCategory;
  aiModel: AiModel;
  likeCount: number;
  bookmarkCount: number;
  copyCount: number;
  liked: boolean;
  /** Matches backend PromptResponse record field name (`bookmarked`). */
  bookmarked: boolean;
  createdAt: string;
  updatedAt: string;
}

export interface PromptToggleResponse {
  active: boolean;
  count: number;
}
