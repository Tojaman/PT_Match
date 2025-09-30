package com.solo.ptmatch.trainer.presentation.request;

import java.time.LocalDate;

import com.solo.ptmatch.trainer.domain.Certification;
import com.solo.ptmatch.trainer.domain.TrainerProfile;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Schema(description = "트레이너 자격증 요청")
public record TrainerCertificationRequest(
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
    public Certification toEntity(TrainerProfile savedProfile) {

        return Certification.create(
            savedProfile,
            name,
            issuingOrganization,
            acquisitionDate
        );
    }
}