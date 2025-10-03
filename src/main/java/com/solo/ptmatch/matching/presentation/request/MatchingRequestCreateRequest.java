package com.solo.ptmatch.matching.presentation.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.apache.catalina.UserDatabase;
import org.springframework.cglib.core.Local;
import org.springframework.security.config.annotation.web.configurers.oauth2.client.OAuth2LoginConfigurer;

import java.time.LocalDateTime;

@Schema(description = "매칭 신청 요청")
public record MatchingRequestCreateRequest(
    @Schema(description = "신청할 상품 ID", example = "1")
    @NotNull
    Long productId,

    Long trainerProfileId,

    LocalDateTime startTime,
    LocalDateTime endTile,

    @Schema(description = "신청 메시지", example = "주 2회 PT 받고 싶습니다. 시간 조율 원합니다.")
    @NotBlank
    @Size(max = 500)
    String message,

    UserInfo userInfo
) {
}
