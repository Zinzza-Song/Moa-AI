package com.songzinzza.moaai.global.storage;

import org.springframework.web.multipart.MultipartFile;

public interface ImageStorageService {
  String store(MultipartFile file, String directory);

  void delete(String fileUrl);
}
