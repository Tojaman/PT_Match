package com.solo.ptmatch.trainer.application.dto;

import java.util.List;

public record TrainerProfileUpsertCommand(
    String bio,
    int careerYears,
    List<String> specialties,
    String gymAddress,
    String profileImageUrl,
    List<TrainerCertificationCommand> certifications
) {
}
