package com.songzinzza.moaai.global.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;

@Configuration
public class SwaggerConfig { // Swagger UI 우측 상단에 Authorize 버튼 활성화
  @Bean
  public OpenAPI openAPI() {
    String securitySchemeName = "BeareAuth";

    return new OpenAPI()
        .info(
            new Info()
                .title("모아AI(MoaAI) API 명세서")
                .description("AI 트렌드 요약 & 프롬프트 공유 플랫폼 백엔드 API")
                .version("v1.0.0"))
        .addSecurityItem(new SecurityRequirement().addList(securitySchemeName))
        .components(new Components()
            .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                .name(securitySchemeName)
                .type(SecurityScheme.Type.HTTP)
                .scheme("bearer")
                .bearerFormat("JWT")));
  }
}
