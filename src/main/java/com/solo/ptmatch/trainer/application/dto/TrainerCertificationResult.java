package com.solo.ptmatch.trainer.application.dto;

import java.time.LocalDate;

public record TrainerCertificationResult(
    Long certificationId,
    String name,
    String issuingOrganization,
    LocalDate acquisitionDate
) {
}
