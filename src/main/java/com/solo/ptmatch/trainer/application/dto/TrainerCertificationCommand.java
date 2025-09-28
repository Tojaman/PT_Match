package com.solo.ptmatch.trainer.application.dto;

import java.time.LocalDate;

public record TrainerCertificationCommand(
    String name,
    String issuingOrganization,
    LocalDate acquisitionDate
) {
}
