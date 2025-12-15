package com.solo.ptmatch.trainer.presentation.response;

import com.solo.ptmatch.trainer.domain.Program;

public record TrainerProgramResponse(
        Long id,
        String title,
        String content) {
    public static TrainerProgramResponse from(Program program) {
        return new TrainerProgramResponse(
                program.getId(),
                program.getTitle(),
                program.getContent());
    }
}
