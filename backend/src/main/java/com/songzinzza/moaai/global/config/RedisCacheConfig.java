package com.songzinzza.moaai.global.config;

import java.time.Duration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

@Configuration
@EnableCaching
public class RedisCacheConfig {
  public static final String NEWS_LIST = "news:list";
  public static final String NEWS_DETAIL = "news:detail";
  public static final String PROMPT_LIST = "prompt:list";
  public static final String PROMPT_DETAIL = "prompt:detail";

  @Bean
  public RedisCacheManager cacheManager(RedisConnectionFactory connectionFactory) {
    RedisCacheConfiguration defaultConfig = RedisCacheConfiguration.defaultCacheConfig()
        .entryTtl(Duration.ofMinutes(5))
        .disableCachingNullValues()
        .serializeKeysWith(
            RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.string()))
        .serializeValuesWith(
            RedisSerializationContext.SerializationPair.fromSerializer(RedisSerializer.json()));

    Map<String, RedisCacheConfiguration> cacheConfigs = new HashMap<>();
    cacheConfigs.put(NEWS_LIST, defaultConfig.entryTtl(Duration.ofMinutes(10)));
    cacheConfigs.put(NEWS_DETAIL, defaultConfig.entryTtl(Duration.ofMinutes(30)));
    cacheConfigs.put(PROMPT_LIST, defaultConfig.entryTtl(Duration.ofMinutes(5)));
    cacheConfigs.put(PROMPT_DETAIL, defaultConfig.entryTtl(Duration.ofMinutes(10)));

    return RedisCacheManager.builder(connectionFactory)
        .cacheDefaults(defaultConfig)
        .withInitialCacheConfigurations(cacheConfigs)
        .build();
  }
}
