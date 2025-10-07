package com.solo.ptmatch.common.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI openAPI() {
        // API 정보 정의
        Info info = new Info()
                .title("PT Match API")
                .version("v1.0.0")
                .description("PT Match 프로젝트 API 명세서입니다.");

        // Security Scheme 이름 정의
        String securitySchemeName = "bearerAuth";

        // Security Requirement 정의
        SecurityRequirement securityRequirement = new SecurityRequirement().addList(securitySchemeName);

        // Security Scheme 정의
        Components components = new Components()
                .addSecuritySchemes(securitySchemeName, new SecurityScheme()
                        .name(securitySchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT"));

        // OpenAPI 객체 생성 및 설정
        return new OpenAPI()
                .info(info)
                .addSecurityItem(securityRequirement)
                .components(components);
    }
}