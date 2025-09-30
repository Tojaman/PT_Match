package com.solo.ptmatch.trainer.presentation.response;

import java.time.LocalDate;
import io.swagger.v3.oas.annotations.media.Schema;
import com.solo.ptmatch.trainer.domain.Certification;

@Schema(description = "자격증 응답")
public record TrainerCertificationResponse(
    @Schema(description = "자격증 ID", example = "1")
    Long certificationId,
    @Schema(description = "자격증명")
    String name,
    @Schema(description = "발급 기관")
    String issuingOrganization,
    @Schema(description = "취득일", example = "2023-01-01")
    LocalDate acquisitionDate
) {
    public static TrainerCertificationResponse from(Certification certification) {
        return new TrainerCertificationResponse(
                certification.getId(),
                certification.getName(),
                certification.getIssuingOrganization(),
                certification.getAcquisitionDate()
        );
    }
}