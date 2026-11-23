package com.songzinzza.moaai.global.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
public class JpaConfig { // 생성일 수정일 자동 기록 활성화

}
