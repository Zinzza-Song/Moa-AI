import type { AiModel } from './prompt';

export interface PromptRefineRequest {
  originalPrompt: string;
  targetModel?: AiModel;
  goal?: string;
}

export interface PromptRefineResponse {
  originalPrompt: string;
  suggestedTitle: string;
  refinedPrompt: string;
  resultExample: string;
  improvements: string[];
  remainingDailyCount: number;
}
