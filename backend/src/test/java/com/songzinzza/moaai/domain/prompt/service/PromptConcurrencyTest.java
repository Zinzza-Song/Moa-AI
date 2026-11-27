package com.songzinzza.moaai.domain.prompt.service;

import static org.assertj.core.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import com.songzinzza.moaai.domain.prompt.entity.AiModel;
import com.songzinzza.moaai.domain.prompt.entity.Prompt;
import com.songzinzza.moaai.domain.prompt.entity.PromptCategory;
import com.songzinzza.moaai.domain.prompt.repository.PromptLikeRepository;
import com.songzinzza.moaai.domain.prompt.repository.PromptRepository;
import com.songzinzza.moaai.domain.user.entity.Role;
import com.songzinzza.moaai.domain.user.entity.SocialProvider;
import com.songzinzza.moaai.domain.user.entity.User;
import com.songzinzza.moaai.domain.user.repository.UserRepository;

@SpringBootTest
class PromptConcurrencyTest {

  @Autowired
  private PromptService promptService;

  @Autowired
  private PromptRepository promptRepository;

  @Autowired
  private PromptLikeRepository promptLikeRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private TransactionTemplate transactionTemplate;

  private Long promptId;

  private final List<Long> createdUserIds = new ArrayList<>();

  @BeforeEach
  void setUp() {
    User author = userRepository.save(User.builder()
        .email("concurrency-author@moaai.com")
        .nickname("동시성작성자")
        .provider(SocialProvider.values()[0])
        .providerId("test-author-" + UUID.randomUUID())
        .role(Role.USER)
        .build());

    createdUserIds.add(author.getId());

    Prompt prompt = promptRepository.save(Prompt.builder()
        .user(author)
        .title("동시성 테스트 프롬프트")
        .content("동시성 테스트 본문")
        .resultExample("예시")
        .category(PromptCategory.values()[0])
        .aiModel(AiModel.values()[0])
        .build());

    promptId = prompt.getId();
  }

  @AfterEach
  void tearDown() {
    transactionTemplate.executeWithoutResult(status -> {
      if (promptId != null) {
        promptLikeRepository.deleteByPromptId(promptId);
        promptRepository.deleteById(promptId);
      }
      userRepository.deleteAllById(createdUserIds);
    });
    createdUserIds.clear();
  }

  @Test
  @DisplayName("100개의 동시 복사 요청에도 copyCount 누락 없이 정확히 100이 기록")
  void increaseCopyCount_concurrent100Requests() throws InterruptedException {
    int threadCount = 100;
    ExecutorService executor = Executors.newFixedThreadPool(32);
    CountDownLatch latch = new CountDownLatch(threadCount);

    for (int i = 0; i < threadCount; i++) {
      executor.submit(() -> {
        try {
          promptService.increaseCopyCount(promptId);
        } finally {
          latch.countDown();
        }
      });
    }

    latch.await();

    executor.shutdown();

    Long finalCopyCount = promptRepository.findCopyCountById(promptId).orElseThrow();

    assertThat(finalCopyCount).isEqualTo(100L);
  }

  @Test
  @DisplayName("서로 다른 50명의 유저가 동시에 좋아요를 눌러도 likeCount가 정확히 50이 됨")
  void toggleLike_concurrent50Users() throws InterruptedException {
    int userCount = 50;
    List<Long> likerIds = new ArrayList<>();
    for (int i = 0; i < userCount; i++) {
      User u = userRepository.save(User.builder()
          .email("liker" + i + "@moaai.com")
          .nickname("유저" + i)
          .provider(SocialProvider.values()[0])
          .providerId("test-liker-" + UUID.randomUUID())
          .role(Role.USER)
          .build());

      likerIds.add(u.getId());

      createdUserIds.add(u.getId());
    }

    ExecutorService executor = Executors.newFixedThreadPool(32);
    CountDownLatch latch = new CountDownLatch(userCount);

    for (Long userId : likerIds) {
      executor.submit(() -> {
        try {
          promptService.toggleLike(promptId, userId);
        } finally {
          latch.countDown();
        }
      });
    }

    latch.await();

    executor.shutdown();

    Long finalLikeCount = promptRepository.findLikeCountById(promptId).orElseThrow();

    assertThat(finalLikeCount).isEqualTo(50L);
  }
}
