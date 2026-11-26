import type { AiModel } from './prompt';

export interface PromptRefineRequest {
  originalPrompt: string;
  targetModel?: AiModel;
  goal?: string;
}

export interface PromptRefineResponse {
  originalPrompt: string;
  refinedPrompt: string;
  improvements: string[];
  remainingDailyCount: number;
}
