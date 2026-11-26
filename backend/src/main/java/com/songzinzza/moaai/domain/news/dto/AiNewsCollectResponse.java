package com.songzinzza.moaai.domain.news.dto;

public record AiNewsCollectResponse(
    int totalFetched,
    int savedCount,
    int skippedCount,
    int failedCount) {

}
