package com.songzinzza.moaai.domain.ai.dto;

import java.util.List;

public record PromptRefineResponse(
    String originalPrompt,
    String refinedPrompt,
    List<String> improvements,
    long remainingDailyCount) {

}
