package com.solo.ptmatch.trainer.presentation.request;

import com.solo.ptmatch.trainer.application.dto.TrainerCertificationCommand;
import com.solo.ptmatch.trainer.application.dto.TrainerProfileUpsertCommand;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.List;

@Schema(description = "트레이너 프로필 수정 요청")
public record TrainerProfileUpdateRequest(
    @Schema(description = "자기소개", example = "업데이트된 소개")
    @NotBlank
    String bio,

    @Schema(description = "경력 년수", example = "12")
    @Min(0)
    int careerYears,

    @Schema(description = "전문 분야 목록", example = "[\"재활\", \"다이어트\"]")
    @NotEmpty
    List<@NotBlank String> specialties,

    @Schema(description = "활동 지점", example = "서울 서초구 ...")
    @NotBlank
    String gymAddress,

    @Schema(description = "프로필 이미지 URL", example = "https://example.com/new-profile.jpg")
    @NotBlank
    String profileImageUrl,

    @Schema(description = "자격증 목록")
    @NotNull
    @Valid
    List<CertificationRequest> certifications
) {

    public TrainerProfileUpsertCommand toCommand() {
        return new TrainerProfileUpsertCommand(
            bio,
            careerYears,
            specialties,
            gymAddress,
            profileImageUrl,
            certifications.stream()
                .map(CertificationRequest::toCommand)
                .toList()
        );
    }

    @Schema(description = "트레이너 자격증 요청")
    public record CertificationRequest(
        @Schema(description = "자격증명", example = "생활체육지도사 2급")
        @NotBlank
        String name,

        @Schema(description = "발급 기관", example = "대한체육회")
        @NotBlank
        String issuingOrganization,

        @Schema(description = "취득일", example = "2024-05-01")
        @NotNull
        LocalDate acquisitionDate
    ) {

        private TrainerCertificationCommand toCommand() {
            return new TrainerCertificationCommand(name, issuingOrganization, acquisitionDate);
        }
    }
}
