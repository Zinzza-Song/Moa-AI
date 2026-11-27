package com.songzinzza.moaai.domain.ai.dto;

import java.util.List;

public record PromptRefineResponse(
        String originalPrompt,
        String suggestedTitle,
        String refinedPrompt,
        String resultExample,
        List<String> improvements,
        long remainingDailyCount) {

}
