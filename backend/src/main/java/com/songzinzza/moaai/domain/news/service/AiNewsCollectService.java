package com.songzinzza.moaai.domain.news.service;

import java.util.List;
import java.util.regex.Pattern;

import org.springframework.ai.chat.client.ChatClient;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.songzinzza.moaai.domain.news.config.NewsCollectProperties;
import com.songzinzza.moaai.domain.news.dto.AiNewsCollectResponse;
import com.songzinzza.moaai.domain.news.entity.AiNews;
import com.songzinzza.moaai.domain.news.repository.AiNewsRepository;
import com.songzinzza.moaai.domain.news.service.RssFeedParser.RssFeedItem;
import com.songzinzza.moaai.global.config.RedisCacheConfig;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class AiNewsCollectService {
  private static final String SYSTEM_PROMPT = """
      당신은 AI 기술 트렌드 전문 에디터입니다.
      제공된 영문 또는 국문 AI 뉴스 기사의 제목과 내용을 분석하여 한국어로 깔끔하게 정리해 주세요.

      작성 규칙:
      1. title: 기사의 핵심을 잘 드러내는 자연스러운 한국어 제목 (80자 이내)
      2. summary: 개발자와 AI 관심자가 핵심을 빠르게 파악할 수 있도록 3~5문장으로 요약 (한국어)
      3. tags: 핵심 키워드 3~4개를 쉼표로 구분한 문자열 (예: "Gemini,LLM,Agent,멀티모달")
      """;

  private final NewsCollectProperties properties;
  private final RssFeedParser rssFeedParser;
  private final AiNewsRepository aiNewsRepository;
  private final ChatClient chatClient;
  private final CacheManager cacheManager;

  public AiNewsCollectService(
      NewsCollectProperties properties,
      RssFeedParser rssFeedParser,
      AiNewsRepository aiNewsRepository,
      ChatClient.Builder chatClientBuilder,
      CacheManager cacheManager) {
    this.properties = properties;
    this.rssFeedParser = rssFeedParser;
    this.aiNewsRepository = aiNewsRepository;
    this.chatClient = chatClientBuilder.build();
    this.cacheManager = cacheManager;
  }

  public AiNewsCollectResponse collectAll() {
    int totalFetched = 0;
    int savedCount = 0;
    int skippedCount = 0;
    int failedCount = 0;

    for (NewsCollectProperties.FeedSource feed : properties.getFeeds()) {
      boolean hasKeywordFilter = feed.getKeywords() != null && !feed.getKeywords().isEmpty();
      int fetchLimit = hasKeywordFilter ? Math.max(properties.getMaxPerFeed() * 10, 20)
          : properties.getMaxPerFeed();

      List<RssFeedItem> items = rssFeedParser.fetch(
          feed.getName(), feed.getUrl(), fetchLimit);
      int matchedInFeed = 0;

      for (RssFeedItem item : items) {
        if (matchedInFeed >= properties.getMaxPerFeed())
          break;

        if (!matchesKeywords(item, feed.getKeywords()))
          continue;

        matchedInFeed++;
        totalFetched++;

        String url = truncate(item.link(), 500);

        if (aiNewsRepository.existsByOriginalUrl(url)) {
          skippedCount++;

          continue;
        }

        // Gemini 호출 전 Java 단에서 사전 차단(API 비용 낭비 방지)
        if (!isSummarizable(item)) {
          skippedCount++;

          log.info("원문 부족으로 Gemini 호출 생략 - source={}, title={}, descLen={}",
              item.sourceName(),
              item.title(),
              item.description() == null ? 0 : item.description().length());

          continue;
        }

        try {
          AiNewsSummaryOutput summarized = summarizeWithGemini(item);

          AiNews news = AiNews.builder()
              .originalUrl(url)
              .title(truncate(summarized.title(), 255))
              .summary(summarized.summary())
              .tags(truncate(summarized.tags(), 255))
              .sourceName(truncate(item.sourceName(), 100))
              .publishedAt(item.publishedAt())
              .build();

          aiNewsRepository.save(news);

          savedCount++;
        } catch (DataIntegrityViolationException e) {
          skippedCount++;
        } catch (Exception e) {
          failedCount++;

          log.error("AI 뉴스 요약 / 저장 실패 - url={}", url, e);
        }
      }
    }

    log.info("AI 뉴스 수집 완료 - fetched={}, saved={}, skipped={}, failed={}",
        totalFetched, savedCount, skippedCount, failedCount);

    if (savedCount > 0) {
      Cache newsListCache = cacheManager.getCache(RedisCacheConfig.NEWS_LIST);
      if (newsListCache != null)
        newsListCache.clear();
    }

    return new AiNewsCollectResponse(totalFetched, savedCount, skippedCount, failedCount);
  }

  private boolean matchesKeywords(RssFeedItem item, List<String> keywords) {
    if (keywords == null || keywords.isEmpty())
      return true;

    String target = (item.title() != null ? item.title() : "") + " "
        + (item.description() != null ? item.description() : "");

    for (String keyword : keywords) {
      if (keyword == null || keyword.isBlank())
        continue;

      Pattern pattern = Pattern.compile(
          "(?<![A-Za-z0-9])" + Pattern.quote(keyword.trim()) + "(?![A-Za-z0-9])",
          Pattern.CASE_INSENSITIVE);

      if (pattern.matcher(target).find())
        return true;
    }

    return false;
  }

  private AiNewsSummaryOutput summarizeWithGemini(RssFeedItem item) {
    String userPrompt = """
        [출처] %s
        [원문 제목] %s
        [원문 요약] %s
        """.formatted(
        item.sourceName(),
        item.title(),
        truncate(item.description(), 1500));

    AiNewsSummaryOutput output = chatClient.prompt()
        .system(SYSTEM_PROMPT)
        .user(userPrompt)
        .call()
        .entity(AiNewsSummaryOutput.class);

    if (output == null || output.title() == null || output.title().isBlank()
        || output.summary() == null || output.summary().isBlank())
      throw new IllegalStateException("Gemini 뉴스 요약 응답이 비어 있습니다.");

    return output;
  }

  private boolean isSummarizable(RssFeedItem item) {
    if (item.link() == null || !item.link().startsWith("http"))
      return false;

    if (item.title() == null || item.title().trim().length() < 10)
      return false;

    if (item.description() == null || item.description().trim().length() < 50)
      return false;

    return true;
  }

  private String truncate(String value, int maxLength) {
    if (value == null)
      return "";

    return value.length() <= maxLength ? value : value.substring(0, maxLength);
  }

  public record AiNewsSummaryOutput(
      String title,
      String summary,
      String tags) {
  }
}
