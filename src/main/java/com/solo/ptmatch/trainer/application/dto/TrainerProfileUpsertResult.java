package com.solo.ptmatch.trainer.application.dto;

import java.util.List;

public record TrainerProfileUpsertResult(
    Long profileId,
    String bio,
    int careerYears,
    List<String> specialties,
    String gymAddress,
    String profileImageUrl
) {
}
