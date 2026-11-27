package com.songzinzza.moaai.domain.news.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Answers;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import com.songzinzza.moaai.domain.news.config.NewsCollectProperties;
import com.songzinzza.moaai.domain.news.dto.AiNewsCollectResponse;
import com.songzinzza.moaai.domain.news.entity.AiNews;
import com.songzinzza.moaai.domain.news.repository.AiNewsRepository;
import com.songzinzza.moaai.domain.news.service.AiNewsCollectService.AiNewsSummaryOutput;
import com.songzinzza.moaai.domain.news.service.RssFeedParser.RssFeedItem;
import com.songzinzza.moaai.global.config.RedisCacheConfig;

@ExtendWith(MockitoExtension.class)
class AiNewsCollectServiceTest {
  @Mock
  private NewsCollectProperties properties;

  @Mock
  private RssFeedParser rssFeedParser;

  @Mock
  private AiNewsRepository aiNewsRepository;

  @Mock
  private ChatClient.Builder chatClientBuilder;

  @Mock(answer = Answers.RETURNS_DEEP_STUBS)
  private ChatClient chatClient;

  @Mock
  private CacheManager cacheManager;

  @Mock
  private Cache newsListCache;

  private AiNewsCollectService collectService;

  @BeforeEach
  void setUp() {
    given(chatClientBuilder.build()).willReturn(chatClient);

    collectService = new AiNewsCollectService(
        properties, rssFeedParser, aiNewsRepository, chatClientBuilder, cacheManager);
  }

  @Test
  @DisplayName("신규 AI 기사가 1건 저장되면 news:list 캐시를 무효화")
  void collectAll_savesNewArticle_andClearsCache() {
    NewsCollectProperties.FeedSource feed = mockFeed("TechCrunch", "https://rss.test/ai", List.of("AI", "LLM"));
    RssFeedItem validItem = mockItem(
        "https://news.test/1",
        "New Gemini AI model released for developers",
        "Google announced a major update to its Gemini AI model with improved coding and reasoning capabilities for developers.",
        "TechCrunch");

    given(properties.getFeeds()).willReturn(List.of(feed));
    given(properties.getMaxPerFeed()).willReturn(5);
    given(rssFeedParser.fetch(anyString(), anyString(), anyInt())).willReturn(List.of(validItem));
    given(aiNewsRepository.existsByOriginalUrl("https://news.test/1")).willReturn(false);
    given(chatClient.prompt().system(anyString()).user(anyString()).call().entity(AiNewsSummaryOutput.class))
        .willReturn(new AiNewsSummaryOutput("구글 신형 Gemini 모델 공개", "핵심 요약 내용입니다.", "Gemini,AI,LLM"));
    given(cacheManager.getCache(RedisCacheConfig.NEWS_LIST)).willReturn(newsListCache);

    AiNewsCollectResponse result = collectService.collectAll();

    assertThat(result.totalFetched()).isEqualTo(1);
    assertThat(result.savedCount()).isEqualTo(1);
    assertThat(result.skippedCount()).isEqualTo(0);
    assertThat(result.failedCount()).isEqualTo(0);
    verify(aiNewsRepository).save(any(AiNews.class));
    verify(newsListCache).clear();
  }

  @Test
  @DisplayName("키워드 불일치 중복 URL 원문 부족 기사는 Gemini 호출 없이 스킵하고 캐시를 유지")
  void collectAll_skipsFilteredDuplicateAndShortItems_withoutClearingCache() {
    NewsCollectProperties.FeedSource feed = mockFeed("TechCrunch", "https://rss.test/ai", List.of("AI"));

    // 1) 키워드 불일치 (AI 없음 -> matchedInFeed 자체 제외)
    RssFeedItem unmatched = mockItem(
        "https://news.test/unmatched",
        "New electric car launches this summer",
        "An automotive company revealed its latest electric SUV with longer battery range and faster charging.",
        "TechCrunch");

    // 2) 중복 URL
    RssFeedItem duplicate = mockItem(
        "https://news.test/dup",
        "AI startup raises new funding round",
        "The AI startup secured significant Series B investment to expand its enterprise inference infrastructure.",
        "TechCrunch");

    // 3) 원문 설명 50자 미만(사전 차단)
    RssFeedItem tooShort = mockItem(
        "https://news.test/short",
        "AI brief update for today",
        "Short description.",
        "TechCrunch");

    given(properties.getFeeds()).willReturn(List.of(feed));
    given(properties.getMaxPerFeed()).willReturn(5);
    given(rssFeedParser.fetch(anyString(), anyString(), anyInt())).willReturn(List.of(unmatched, duplicate, tooShort));
    given(aiNewsRepository.existsByOriginalUrl("https://news.test/dup")).willReturn(true);
    given(aiNewsRepository.existsByOriginalUrl("https://news.test/short")).willReturn(false);

    AiNewsCollectResponse result = collectService.collectAll();

    assertThat(result.totalFetched()).isEqualTo(2);
    assertThat(result.savedCount()).isEqualTo(0);
    assertThat(result.skippedCount()).isEqualTo(2);
    assertThat(result.failedCount()).isEqualTo(0);
    verify(aiNewsRepository, never()).save(any());
    verify(cacheManager, never()).getCache(anyString());
  }

  @Test
  @DisplayName("Gemini 요약 중 예외가 발생하면 failed 카운트를 증가시키고 배치를 중단하지 않음")
  void collectAll_geminiError_incrementsFailedCount() {
    NewsCollectProperties.FeedSource feed = mockFeed("TechCrunch", "https://rss.test/ai", List.of());
    RssFeedItem item = mockItem(
        "https://news.test/err",
        "Open source LLM benchmark analysis 2026",
        "A comprehensive benchmark comparing major open source large language models across reasoning and coding tasks.",
        "TechCrunch");

    given(properties.getFeeds()).willReturn(List.of(feed));
    given(properties.getMaxPerFeed()).willReturn(5);
    given(rssFeedParser.fetch(anyString(), anyString(), anyInt())).willReturn(List.of(item));
    given(aiNewsRepository.existsByOriginalUrl("https://news.test/err")).willReturn(false);
    given(chatClient.prompt().system(anyString()).user(anyString()).call().entity(AiNewsSummaryOutput.class))
        .willThrow(new RuntimeException("Gemini API timeout"));

    AiNewsCollectResponse result = collectService.collectAll();

    assertThat(result.totalFetched()).isEqualTo(1);
    assertThat(result.savedCount()).isEqualTo(0);
    assertThat(result.failedCount()).isEqualTo(1);
    verify(cacheManager, never()).getCache(anyString());
  }

  private NewsCollectProperties.FeedSource mockFeed(String name, String url, List<String> keywords) {
    NewsCollectProperties.FeedSource feed = mock(NewsCollectProperties.FeedSource.class);

    lenient().when(feed.getName()).thenReturn(name);
    lenient().when(feed.getUrl()).thenReturn(url);
    lenient().when(feed.getKeywords()).thenReturn(keywords);

    return feed;
  }

  private RssFeedItem mockItem(String link, String title, String description, String sourceName) {
    RssFeedItem item = mock(RssFeedItem.class);

    lenient().when(item.link()).thenReturn(link);
    lenient().when(item.title()).thenReturn(title);
    lenient().when(item.description()).thenReturn(description);
    lenient().when(item.sourceName()).thenReturn(sourceName);
    lenient().when(item.publishedAt()).thenReturn(LocalDateTime.now());

    return item;
  }
}
