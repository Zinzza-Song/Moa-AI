package com.songzinzza.moaai.global.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Set;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.songzinzza.moaai.global.exception.BusinessException;
import com.songzinzza.moaai.global.exception.ErrorCode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class LocalImageStorageService implements ImageStorageService {
  public static final String URL_PREFIX = "/uploads/";
  private static final long MAX_FILE_SIZE = 5L * 1024 * 1024; // 5mb
  private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
  private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
      "image/jpeg", "image/png", "image/webp");
  private final Path rootLocation;

  public LocalImageStorageService(@Value("${app.upload.dir:uploads}") String uploadDir) {
    this.rootLocation = Paths.get(uploadDir).toAbsolutePath().normalize();
  }

  @Override
  public String store(MultipartFile file, String directory) {
    validateFile(file);

    String ext = extractExtension(file.getOriginalFilename());
    String storedFileName = UUID.randomUUID() + "." + ext;

    Path targetDir = rootLocation.resolve(directory).normalize();
    if (!targetDir.startsWith(rootLocation))
      throw new BusinessException(ErrorCode.INVALID_FILE);

    try {
      Files.createDirectories(targetDir);

      Path destination = targetDir.resolve(storedFileName).normalize();

      Files.copy(file.getInputStream(), destination, StandardCopyOption.REPLACE_EXISTING);

      return URL_PREFIX + directory + "/" + storedFileName;
    } catch (IOException e) {
      log.error("로컬 이미지 저장 실패 - directory={}, fileName={}", directory, storedFileName, e);

      throw new BusinessException(ErrorCode.FILE_UPLOAD_FAILED);
    }
  }

  @Override
  public void delete(String fileUrl) {
    if (fileUrl == null || !fileUrl.startsWith(URL_PREFIX))
      return;

    String relativePath = fileUrl.substring(URL_PREFIX.length());
    Path targetFile = rootLocation.resolve(relativePath).normalize();

    if (!targetFile.startsWith(rootLocation))
      return;

    try {
      Files.deleteIfExists(targetFile);
    } catch (IOException e) {
      log.warn("기존 로컬 이미지 삭제 실패 - path={}", targetFile, e);
    }
  }

  private void validateFile(MultipartFile file) {
    if (file == null || file.isEmpty() || file.getSize() > MAX_FILE_SIZE)
      throw new BusinessException(ErrorCode.INVALID_FILE);

    String contentType = file.getContentType();
    if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase()))
      throw new BusinessException(ErrorCode.INVALID_FILE);

    String ext = extractExtension(file.getOriginalFilename());
    if (!ALLOWED_EXTENSIONS.contains(ext))
      throw new BusinessException(ErrorCode.INVALID_FILE);
  }

  private String extractExtension(String originalFilename) {
    if (originalFilename == null || !originalFilename.contains("."))
      return "";

    return originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
  }
}
