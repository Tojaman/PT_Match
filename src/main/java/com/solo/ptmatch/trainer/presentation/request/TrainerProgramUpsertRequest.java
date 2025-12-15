package com.solo.ptmatch.trainer.presentation.request;

import com.solo.ptmatch.trainer.domain.Program;
import com.solo.ptmatch.trainer.domain.TrainerProfile;

import jakarta.validation.constraints.NotBlank;

public record TrainerProgramUpsertRequest(
        @NotBlank(message = "프로그램 제목은 필수입니다.") String title,

        @NotBlank(message = "프로그램 내용은 필수입니다.") String content)
{
    public Program toEntity(TrainerProfile trainerProfile) {
        return new Program(trainerProfile, title, content);
    }
}
