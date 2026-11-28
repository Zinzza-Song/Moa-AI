package com.songzinzza.moaai.global.storage;

import static org.assertj.core.api.Assertions.*;

import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;

import com.songzinzza.moaai.global.exception.BusinessException;
import com.songzinzza.moaai.global.exception.ErrorCode;

class LocalImageStorageServiceTest {

  @TempDir
  Path tempDir;

  @Test
  @DisplayName("유효한 PNG 이미지를 저장하고 삭제")
  void storeAndDelete_validImage() {
    LocalImageStorageService storage = new LocalImageStorageService(tempDir.toString());

    MockMultipartFile file = new MockMultipartFile(
        "file", "avatar.png", "image/png", new byte[] { 1, 2, 3, 4 });

    String url = storage.store(file, "profiles");
    String relativePath = url.substring(LocalImageStorageService.URL_PREFIX.length());

    assertThat(url).startsWith("/uploads/profiles/").endsWith(".png");
    assertThat(Files.exists(tempDir.resolve(relativePath))).isTrue();

    storage.delete(url);
    assertThat(Files.exists(tempDir.resolve(relativePath))).isFalse();
  }

  @Test
  @DisplayName("허용되지 않은 파일 확장자/MIME 타입 업로드 시 INVALID_FILE 예외가 발생")
  void store_invalidFileType_throwsException() {
    LocalImageStorageService storage = new LocalImageStorageService(tempDir.toString());

    MockMultipartFile file = new MockMultipartFile(
        "file", "script.exe", "application/octet-stream", new byte[] { 1, 2, 3 });

    assertThatThrownBy(() -> storage.store(file, "profiles"))
        .isInstanceOf(BusinessException.class)
        .extracting("errorCode")
        .isEqualTo(ErrorCode.INVALID_FILE);
  }
}
